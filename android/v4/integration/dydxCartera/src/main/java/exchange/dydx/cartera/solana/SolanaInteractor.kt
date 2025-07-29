package exchange.dydx.dydxCartera.solana
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.solana.publickey.SolanaPublicKey
import com.solana.transaction.AccountMeta
import com.solana.transaction.TransactionInstruction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import timber.log.Timber
import kotlin.math.max
import kotlin.math.pow

class SolanaInteractor(
    private val rpcUrl: String,
) {
    private val TAG = "SolanaInteractor"

    companion object {
        val mainnetUrl = "https://api.mainnet-beta.solana.com"
        val devnetUrl = "https://api.devnet.solana.com"
    }

    suspend fun getRecentBlockhash(): LatestBlockhashResult? = withContext(Dispatchers.IO) {
        val gson = Gson()
        val client = OkHttpClient()

        val json = mapOf(
            "jsonrpc" to "2.0",
            "id" to 1,
            "method" to "getLatestBlockhash",
        )

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            gson.toJson(json),
        )

        val request = Request.Builder()
            .url(rpcUrl)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Timber.tag(TAG).e("Request failed: ${response.code}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            return@withContext gson.fromJson(
                responseBody,
                LatestBlockhashResponse::class.java,
            ).result
        } catch (e: Exception) {
            Timber.tag(TAG).e("Request failed: ${e.message}")
            return@withContext null
        }
    }

    suspend fun getBalance(publicKey: String): Double? = withContext(Dispatchers.IO) {
        val gson = Gson()
        val client = OkHttpClient()

        val json = mapOf(
            "jsonrpc" to "2.0",
            "id" to 1,
            "method" to "getBalance",
            "params" to listOf(publicKey),
        )

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            gson.toJson(json),
        )

        val request = Request.Builder()
            .url(rpcUrl)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Timber.tag(TAG).e("Request failed: ${response.code}")
                return@withContext null
            }

            val body = response.body?.string() ?: return@withContext null
            val parsed = gson.fromJson(body, BalanceResponse::class.java)
            return@withContext parsed.result.value.toDouble() / 10.0.pow(9.0)
        } catch (e: Exception) {
            Timber.tag(TAG).e("Request failed: ${e.message}")
            return@withContext null
        }
    }

    suspend fun getTokenBalance(publicKey: String, tokenAddress: String): Double? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val gson = Gson()

        val json = mapOf(
            "jsonrpc" to "2.0",
            "id" to 1,
            "method" to "getTokenAccountsByOwner",
            "params" to listOf(
                publicKey,
                mapOf(
                    "mint" to tokenAddress,
                ),
                mapOf("encoding" to "jsonParsed"),
            ),
        )

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            gson.toJson(json),
        )

        val request = Request.Builder()
            .url(rpcUrl)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Timber.tag(TAG).e("Request failed: ${response.code}")
                return@withContext null
            }

            try {
                val parsed =
                    gson.fromJson(response.body?.string(), TokenAccountsResponse::class.java)
                var balance = 0.0f
                for (account in parsed.result.value) {
                    val tokenAmount = account.account.data.parsed.info.tokenAmount.uiAmount
                    balance = max(balance, tokenAmount)
                }
                return@withContext balance.toDouble()
            } catch (e: Exception) {
                Timber.tag(TAG).e("Failed to parse response: ${e.message}")
                return@withContext null
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e("Request failed: ${e.message}")
            return@withContext null
        }
    }

    suspend fun sendRawTransaction(base58Tx: String): String? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val gson = Gson()

        val json = mapOf(
            "jsonrpc" to "2.0",
            "id" to 1,
            "method" to "sendTransaction",
            "params" to listOf(base58Tx, mapOf("encoding" to "base58")),
        )

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            gson.toJson(json),
        )

        val request = Request.Builder()
            .url(rpcUrl)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Request failed with code: ${response.code}")
        }

        val jsonRespsonse = response.body?.string() ?: run {
            throw Exception("Response body is null or empty")
        }
        val parsed =
            gson.fromJson(jsonRespsonse, SendTransactionResponse::class.java)
        if (parsed.error != null) {
            throw Exception(
                "Error in response: ${parsed.error.code} - ${parsed.error.message}",
            )
        }
        return@withContext parsed.result
    }

    fun buildTestMemoTransaction(address: SolanaPublicKey, memo: String) =
        TransactionInstruction(
            programId = SystemProgram.programId,
            accounts = listOf(AccountMeta(publicKey = address, isSigner = true, isWritable = true)),
            data = memo.encodeToByteArray(),
        )
}

@Serializable
data class LatestBlockhashResponse(
    val result: LatestBlockhashResult
)

@Serializable
data class LatestBlockhashResult(
    val context: ContextInfo,
    val value: BlockhashValue
)

@Serializable
data class ContextInfo(
    val slot: Long
)

@Serializable
data class BlockhashValue(
    @SerializedName("blockhash") val blockhash: String,
    @SerializedName("lastValidBlockHeight") val lastValidBlockHeight: Long
)

@Serializable
data class BalanceResponse(
    val result: BalanceResult
)

@Serializable
data class BalanceResult(
    val context: ContextInfo,
    val value: Long // balance in lamports
)

@Serializable
data class TokenAccountsResponse(
    val result: ResultWrapper
)

@Serializable
data class ResultWrapper(
    val context: Context,
    val value: List<TokenAccount>
)

@Serializable
data class Context(
    val slot: ULong
)

@Serializable
data class TokenAccount(
    val pubkey: String,
    val account: AccountDetails
)

@Serializable
data class AccountDetails(
    val data: AccountData,
    val executable: Boolean,
    val lamports: ULong,
    val owner: String,
    val rentEpoch: Float
)

@Serializable
data class AccountData(
    val program: String,
    val parsed: ParsedData,
    val space: Int
)

@Serializable
data class ParsedData(
    val type: String,
    val info: TokenInfo
)

@Serializable
data class TokenInfo(
    val mint: String,
    val owner: String,
    val tokenAmount: TokenAmount
)

@Serializable
data class TokenAmount(
    val amount: String,
    val decimals: Int,
    val uiAmount: Float
)

@Serializable
data class RpcError(
    val code: Int?,
    val message: String?
)

@Serializable
data class SendTransactionResponse(
    val jsonrpc: String,
    val result: String?, // the transaction signature (base58)
    val error: RpcError?,
    val id: Int
)
