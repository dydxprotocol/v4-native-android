package exchange.dydx.dydxCartera.walletprovider.providers

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.iwebpp.crypto.TweetNaclFast
import exchange.dydx.dydxCartera.CarteraErrorCode
import exchange.dydx.dydxCartera.PhantomWalletConfig
import exchange.dydx.dydxCartera.decodeBase58
import exchange.dydx.dydxCartera.encodeToBase58String
import exchange.dydx.dydxCartera.entities.Wallet
import exchange.dydx.dydxCartera.solana.SolanaInteractor
import exchange.dydx.dydxCartera.tag
import exchange.dydx.dydxCartera.typeddata.WalletTypedDataProviderProtocol
import exchange.dydx.dydxCartera.typeddata.typedDataAsString
import exchange.dydx.dydxCartera.walletprovider.EthereumAddChainRequest
import exchange.dydx.dydxCartera.walletprovider.WalletConnectCompletion
import exchange.dydx.dydxCartera.walletprovider.WalletConnectedCompletion
import exchange.dydx.dydxCartera.walletprovider.WalletError
import exchange.dydx.dydxCartera.walletprovider.WalletInfo
import exchange.dydx.dydxCartera.walletprovider.WalletOperationCompletion
import exchange.dydx.dydxCartera.walletprovider.WalletOperationProviderProtocol
import exchange.dydx.dydxCartera.walletprovider.WalletOperationStatus
import exchange.dydx.dydxCartera.walletprovider.WalletRequest
import exchange.dydx.dydxCartera.walletprovider.WalletState
import exchange.dydx.dydxCartera.walletprovider.WalletStatusDelegate
import exchange.dydx.dydxCartera.walletprovider.WalletStatusImp
import exchange.dydx.dydxCartera.walletprovider.WalletStatusProtocol
import exchange.dydx.dydxCartera.walletprovider.WalletTransactionRequest
import exchange.dydx.dydxCartera.walletprovider.WalletUserConsentProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random

class PhantomWalletProvider(
    private val phantomWalletConfig: PhantomWalletConfig,
    private val application: Application,
) : WalletOperationProviderProtocol {

    private enum class CallbackAction(val request: String) {
        onConnect("connect"),
        onDisconnect("disconnect"),
        onSignMessage("signMessage"),
        onSignTransaction("signTransaction"),
    }

    private var _walletStatus = WalletStatusImp()
        set(value) {
            field = value
            walletStatusDelegate?.statusChanged(value)
        }
    override val walletStatus: WalletStatusProtocol
        get() = _walletStatus

    override var walletStatusDelegate: WalletStatusDelegate? = null
    override var userConsentDelegate: WalletUserConsentProtocol? = null

    private val baseUrlString = "https://phantom.app/ul/v1"

    private var publicKey: ByteArray? = null
    private var privateKey: ByteArray? = null
    private var phantomPublicKey: ByteArray? = null
    private var session: String? = null

    private var connectionCompletion: WalletConnectCompletion? = null
    private var connectionWallet: Wallet? = null
    private var operationCompletion: WalletOperationCompletion? = null

    override fun handleResponse(uri: Uri): Boolean {
        if (!uri.toString().startsWith(phantomWalletConfig.callbackUrl)) {
            return false
        }

        val action = uri.lastPathSegment ?: return false
        val errorCode = uri.getQueryParameter("errorCode")
        val errorMessage = uri.getQueryParameter("errorMessage") ?: "Unknown error"

        when (action) {
            CallbackAction.onConnect.name -> {
                if (connectionCompletion != null) {
                    if (errorCode != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            connectionCompletion?.invoke(
                                null,
                                WalletError(CarteraErrorCode.CONNECTION_FAILED, errorMessage),
                            )
                            connectionCompletion = null
                        }
                    } else {
                        val encodedPublicKey =
                            uri.getQueryParameter("phantom_encryption_public_key")
                        phantomPublicKey = encodedPublicKey?.decodeBase58()

                        val data = decryptPayload(
                            payload = uri.getQueryParameter("data"),
                            nonce = uri.getQueryParameter("nonce"),
                        )
                        val response = try {
                            Gson().fromJson(data?.decodeToString(), ConnectResponse::class.java)
                        } catch (e: Exception) {
                            null
                        }
                        if (response != null) {
                            session = response.session
                            val walletInfo = WalletInfo(
                                address = response.publicKey,
                                chainId = null,
                                wallet = connectionWallet,
                            )
                            _walletStatus.state = WalletState.CONNECTED_TO_WALLET
                            _walletStatus.connectedWallet = walletInfo
                            CoroutineScope(Dispatchers.Main).launch {
                                connectionCompletion?.invoke(walletInfo, null)
                                connectionCompletion = null
                            }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                connectionCompletion?.invoke(
                                    null,
                                    WalletError(
                                        CarteraErrorCode.UNEXPECTED_RESPONSE,
                                        "Failed to decrypt payload",
                                    ),
                                )
                                connectionCompletion = null
                            }
                        }
                    }
                }
            }

            CallbackAction.onDisconnect.name -> {
                if (errorCode != null) {
                    Timber.tag(tag(this@PhantomWalletProvider)).d("Disconnected Error: $errorMessage, $errorCode")
                }
            }

            CallbackAction.onSignMessage.name -> {
                if (operationCompletion != null) {
                    if (errorCode != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            operationCompletion?.invoke(
                                null,
                                WalletError(CarteraErrorCode.UNEXPECTED_RESPONSE, errorMessage),
                            )
                            operationCompletion = null
                        }
                    } else {
                        val data = decryptPayload(
                            payload = uri.getQueryParameter("data"),
                            nonce = uri.getQueryParameter("nonce"),
                        )
                        val response = try {
                            Gson().fromJson(data?.decodeToString(), SignMessageResponse::class.java)
                        } catch (e: Exception) {
                            null
                        }
                        if (response != null) {
                            CoroutineScope(Dispatchers.Main).launch {
                                operationCompletion?.invoke(response.signature, null)
                                operationCompletion = null
                            }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                operationCompletion?.invoke(
                                    null,
                                    WalletError(
                                        CarteraErrorCode.UNEXPECTED_RESPONSE,
                                        "Failed to decrypt payload",
                                    ),
                                )
                                operationCompletion = null
                            }
                        }
                    }
                }
            }

            CallbackAction.onSignTransaction.name -> {
                if (operationCompletion != null) {
                    if (errorCode != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            operationCompletion?.invoke(
                                null,
                                WalletError(CarteraErrorCode.UNEXPECTED_RESPONSE, errorMessage),
                            )
                            operationCompletion = null
                        }
                    } else {
                        val data = decryptPayload(
                            payload = uri.getQueryParameter("data"),
                            nonce = uri.getQueryParameter("nonce"),
                        )
                        val response = try {
                            Gson().fromJson(data?.decodeToString(), SignTransactionResponse::class.java)
                        } catch (e: Exception) {
                            null
                        }
                        if (response != null) {
                            CoroutineScope(Dispatchers.Main).launch {
                                operationCompletion?.invoke(response.transaction, null)
                                operationCompletion = null
                            }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                operationCompletion?.invoke(
                                    null,
                                    WalletError(
                                        CarteraErrorCode.UNEXPECTED_RESPONSE,
                                        "Failed to decrypt payload",
                                    ),
                                )
                                operationCompletion = null
                            }
                        }
                    }
                }
            }
        }

        return true
    }

    override fun connect(request: WalletRequest, completion: WalletConnectCompletion) {
        if (_walletStatus.state == WalletState.CONNECTED_TO_WALLET) {
            completion(walletStatus.connectedWallet, null)
            return
        }

        val result = TweetNaclFast.Box.keyPair()
        if (result == null) {
            completion(
                null,
                WalletError(CarteraErrorCode.CONNECTION_FAILED, "Failed to generate key pair"),
            )
            return
        }

        publicKey = result.publicKey
        privateKey = result.secretKey

        val publickKeyEncoded = publicKey?.encodeToBase58String()
        if (publickKeyEncoded == null) {
            completion(
                null,
                WalletError(CarteraErrorCode.CONNECTION_FAILED, "Failed to encode public key"),
            )
            return
        }

        val cluster = if (request.chainId == "1") {
            "mainnet-beta"
        } else {
            "devnet"
        }
        if (cluster == null) {
            completion(
                null,
                WalletError(CarteraErrorCode.CONNECTION_FAILED, "Failed to get chainId"),
            )
            return
        }

        try {
            val uri = "$baseUrlString/${CallbackAction.onConnect.request}".toUri()
                .buildUpon()
                .appendQueryParameter("dapp_encryption_public_key", publickKeyEncoded)
                .appendQueryParameter("cluster", cluster)
                .appendQueryParameter("app_url", phantomWalletConfig.appUrl)
                .appendQueryParameter("redirect_link", "${phantomWalletConfig.callbackUrl}/${CallbackAction.onConnect}")
                .build()

            if (openPeerDeeplink(uri)) {
                connectionCompletion = completion
                connectionWallet = request.wallet
            } else {
                completion(
                    null,
                    WalletError(CarteraErrorCode.CONNECTION_FAILED, "Failed to open Phantom app"),
                )
            }
        } catch (e: Exception) {
            completion(
                null,
                WalletError(CarteraErrorCode.CONNECTION_FAILED, e.message ?: "Unknown error"),
            )
        }
    }

    override fun disconnect() {
        publicKey = null
        privateKey = null
        phantomPublicKey = null
        connectionCompletion = null
        operationCompletion = null

        session = null
        connectionWallet = null
        _walletStatus.state = WalletState.IDLE
        _walletStatus.connectedWallet = null
        _walletStatus.connectionDeeplink = null
    }

    override fun signMessage(
        request: WalletRequest,
        message: String,
        connected: WalletConnectedCompletion?,
        status: WalletOperationStatus?,
        completion: WalletOperationCompletion
    ) {
        connect(request = request) { info, error ->
            if (error != null) {
                completion(null, error)
            } else {
                connected?.invoke(info)
                doSignMessage(message, completion)
            }
        }
    }

    private fun doSignMessage(
        message: String,
        completion: WalletOperationCompletion
    ) {
        val request = SignMessageRequest(
            session = session,
            message = message.toByteArray().encodeToBase58String(),
            display = "utf8",
        )
        val uri = createRequestUri(
            request = Gson().toJson(request),
            action = CallbackAction.onSignMessage,
        )
        if (uri != null) {
            if (openPeerDeeplink(uri)) {
                operationCompletion = completion
            } else {
                completion(
                    null,
                    WalletError(CarteraErrorCode.UNEXPECTED_RESPONSE, "Failed to open Phantom app"),
                )
            }
        } else {
            completion(
                null,
                WalletError(CarteraErrorCode.UNEXPECTED_RESPONSE, "Failed to create request URI"),
            )
        }
    }

    override fun sign(
        request: WalletRequest,
        typedDataProvider: WalletTypedDataProviderProtocol?,
        connected: WalletConnectedCompletion?,
        status: WalletOperationStatus?,
        completion: WalletOperationCompletion
    ) {
        val message = typedDataProvider?.typedDataAsString
        if (message == null) {
            completion(
                null,
                WalletError(CarteraErrorCode.UNEXPECTED_RESPONSE, "Typed data is null"),
            )
            return
        }
        signMessage(
            request = request,
            message = message,
            connected = connected,
            status = status,
            completion = completion,
        )
    }

    private fun doSignTransaction(
        transaction: String,
        completion: WalletOperationCompletion
    ) {
        val request = SignTransactionRequest(
            session = session,
            transaction = transaction,
        )
        val uri = createRequestUri(
            request = Gson().toJson(request),
            action = CallbackAction.onSignTransaction,
        )
        if (uri != null) {
            if (openPeerDeeplink(uri)) {
                operationCompletion = completion
            } else {
                completion(
                    null,
                    WalletError(CarteraErrorCode.UNEXPECTED_RESPONSE, "Failed to open Phantom app"),
                )
            }
        } else {
            completion(
                null,
                WalletError(CarteraErrorCode.UNEXPECTED_RESPONSE, "Failed to create request URI"),
            )
        }
    }

    override fun send(
        request: WalletTransactionRequest,
        connected: WalletConnectedCompletion?,
        status: WalletOperationStatus?,
        completion: WalletOperationCompletion
    ) {
        connect(request = request.walletRequest) { info, error ->
            if (error != null) {
                completion(null, error)
            } else {
                connected?.invoke(info)
                doSend(request, completion)
            }
        }
    }

    private fun doSend(
        request: WalletTransactionRequest,
        completion: WalletOperationCompletion
    ) {
        val data = request.solana
        if (data == null) {
            completion(
                null,
                WalletError(CarteraErrorCode.UNEXPECTED_RESPONSE, "Solana transaction data is null"),
            )
            return
        }

        doSignTransaction(transaction = data.encodeToBase58String()) { signed, error ->
            if (error != null || signed == null) {
                completion(null, error)
                return@doSignTransaction
            }

            val isMainnet = request.walletRequest.chainId == "1"
            val solanaInteractor = SolanaInteractor(
                rpcUrl = if (isMainnet) {
                    phantomWalletConfig.solanaMainnetUrl ?: SolanaInteractor.mainnetUrl
                } else {
                    phantomWalletConfig.solanaTestnetUrl ?: SolanaInteractor.devnetUrl
                },
            )

            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    // Solana PRCs can be slower than Skip's, so we wait a bit before sending the transaction
                    Thread.sleep(5000)

                    val hash = solanaInteractor.sendRawTransaction(signed)
                    CoroutineScope(Dispatchers.Main).launch {
                        if (hash != null) {
                            completion(hash, null)
                        } else {
                            completion(
                                null,
                                WalletError(
                                    CarteraErrorCode.UNEXPECTED_RESPONSE,
                                    "Failed to send transaction",
                                ),
                            )
                        }
                    }
                } catch (e: Exception) {
                    Timber.tag(tag(this@PhantomWalletProvider)).e("Failed to send transaction: ${e.message}")
                    CoroutineScope(Dispatchers.Main).launch {
                        completion(
                            null,
                            WalletError(
                                CarteraErrorCode.UNEXPECTED_RESPONSE,
                                "Failed to send transaction: ${e.message}",
                            ),
                        )
                    }
                    return@launch
                }
            }
        }
    }

    override fun addChain(
        request: WalletRequest,
        chain: EthereumAddChainRequest,
        connected: WalletConnectedCompletion?,
        status: WalletOperationStatus?,
        completion: WalletOperationCompletion
    ) {
        TODO("Not yet implemented")
    }

    private fun openPeerDeeplink(uri: Uri): Boolean {
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
            return true
        } catch (exception: ActivityNotFoundException) {
            Timber.tag(tag(this@PhantomWalletProvider)).d("There is no app to handle deep link")
            return false
        }
    }

    private fun decryptPayload(payload: String?, nonce: String?): ByteArray? {
        val decodedData = payload?.decodeBase58() ?: return null
        val decodedNonceData = nonce?.decodeBase58() ?: return null
        val publicKey = phantomPublicKey ?: return null
        val privateKey = privateKey ?: return null

        val box = TweetNaclFast.Box(publicKey, privateKey)
        return box.open(decodedData, decodedNonceData)
    }

    private fun encryptPayload(payload: ByteArray?): Pair<ByteArray, ByteArray>? {
        val payload = payload ?: return null
        val publicKey = phantomPublicKey ?: return null
        val privateKey = privateKey ?: return null
        val nonceData: ByteArray = generateRandomBytes(length = 24)

        val box = TweetNaclFast.Box(publicKey, privateKey)
        val encryptedData = box.box(payload, nonceData)
        return Pair(encryptedData, nonceData)
    }

    private fun generateRandomBytes(length: Int): ByteArray {
        return Random.Default.nextBytes(length)
    }

    private fun createRequestUri(request: String?, action: CallbackAction): Uri? {
        val result = encryptPayload(request?.toByteArray()) ?: return null
        val publicKey = publicKey ?: return null
        val payload = result.first
        val nonce = result.second
        try {
            val uri = "$baseUrlString/${action.request}".toUri()
                .buildUpon()
                .appendQueryParameter("payload", payload.encodeToBase58String())
                .appendQueryParameter("nonce", nonce.encodeToBase58String())
                .appendQueryParameter(
                    "redirect_link",
                    "${phantomWalletConfig.callbackUrl}/${action.name}",
                )
                .appendQueryParameter(
                    "dapp_encryption_public_key",
                    publicKey.encodeToBase58String(),
                )
                .build()
            return uri
        } catch (e: Exception) {
            return null
        }
    }
}

data class ConnectResponse(
    @SerializedName("public_key") val publicKey: String?,
    @SerializedName("session") val session: String?
)

data class DisconnectRequest(
    @SerializedName("session") val session: String?
)

data class SignMessageRequest(
    @SerializedName("session") val session: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("display") val display: String? // "utf8" | "hex"
)

data class SignMessageResponse(
    @SerializedName("signature") val signature: String?
)

data class SignTransactionRequest(
    @SerializedName("session") val session: String?,
    @SerializedName("transaction") val transaction: String?
)

data class SignTransactionResponse(
    @SerializedName("transaction") val transaction: String?
)

data class SendTransactionRequest(
    @SerializedName("session") val session: String?,
    @SerializedName("transaction") val transaction: String?
)

data class SendTransactionResponse(
    @SerializedName("signature") val signature: String?
)

data class PhantomSession(
    @SerializedName("app_url") val appUrl: String?,
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("chain") val chain: String?,
    @SerializedName("cluster") val cluster: String?
)
