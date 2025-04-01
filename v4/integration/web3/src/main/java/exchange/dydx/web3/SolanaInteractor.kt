package exchange.dydx.carteraexample.solana
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.solana.programs.SystemProgram
import com.solana.publickey.SolanaPublicKey
import com.solana.transaction.AccountMeta
import com.solana.transaction.TransactionInstruction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import kotlin.math.max
import kotlin.math.pow

class SolanaInteractor(
    private val rpcUrl: String,
) {
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

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return@withContext null
        }

        val responseBody = response.body?.string() ?: return@withContext null
        return@withContext gson.fromJson(responseBody, LatestBlockhashResponse::class.java).result
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

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return@withContext null
        }

        val body = response.body?.string() ?: return@withContext null
        val parsed = gson.fromJson(body, BalanceResponse::class.java)
        return@withContext parsed.result.value.toDouble() / 10.0.pow(9.0)
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

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return@withContext null
        }

        try {
            val parsed = gson.fromJson(response.body?.string(), TokenAccountsResponse::class.java)
            var balance = 0.0f
            for (account in parsed.result.value) {
                val tokenAmount = account.account.data.parsed.info.tokenAmount.uiAmount
                balance = max(balance, tokenAmount)
            }
            return@withContext balance.toDouble()
        } catch (e: Exception) {
            println("Failed to parse response: ${e.message}")
            return@withContext null
        }
    }

    fun buildTestMemoTransaction(address: SolanaPublicKey, memo: String) =
        TransactionInstruction(
            programId = SystemProgram.programId,
            accounts = listOf(AccountMeta(publicKey = address, isSigner = true, isWritable = true)),
            data = memo.encodeToByteArray(),
        )
}

data class LatestBlockhashResponse(
    val result: LatestBlockhashResult
)

data class LatestBlockhashResult(
    val context: ContextInfo,
    val value: BlockhashValue
)

data class ContextInfo(
    val slot: Long
)

data class BlockhashValue(
    @SerializedName("blockhash") val blockhash: String,
    @SerializedName("lastValidBlockHeight") val lastValidBlockHeight: Long
)

data class BalanceResponse(
    val result: BalanceResult
)

data class BalanceResult(
    val context: ContextInfo,
    val value: Long // balance in lamports
)

data class TokenAccountsResponse(
    val result: ResultWrapper
)

data class ResultWrapper(
    val context: Context,
    val value: List<TokenAccount>
)

data class Context(
    val slot: ULong
)

data class TokenAccount(
    val pubkey: String,
    val account: AccountDetails
)

data class AccountDetails(
    val data: AccountData,
    val executable: Boolean,
    val lamports: ULong,
    val owner: String,
    val rentEpoch: Float
)

data class AccountData(
    val program: String,
    val parsed: ParsedData,
    val space: Int
)

data class ParsedData(
    val type: String,
    val info: TokenInfo
)

data class TokenInfo(
    val mint: String,
    val owner: String,
    val tokenAmount: TokenAmount
)

data class TokenAmount(
    val amount: String,
    val decimals: Int,
    val uiAmount: Float
)
