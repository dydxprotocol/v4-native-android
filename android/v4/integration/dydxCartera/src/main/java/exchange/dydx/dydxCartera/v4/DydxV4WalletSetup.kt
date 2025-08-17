package exchange.dydx.dydxCartera.v4

import android.content.Context
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxCartera.DydxWalletSetup
import exchange.dydx.dydxCartera.decodeBase58
import exchange.dydx.dydxCartera.entities.Wallet
import exchange.dydx.dydxCartera.typeddata.EIP712DomainTypedDataProvider
import exchange.dydx.dydxCartera.typeddata.WalletTypedData
import exchange.dydx.dydxCartera.walletprovider.WalletInfo
import exchange.dydx.dydxCartera.walletprovider.WalletRequest
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientProtocol
import exchange.dydx.utilities.utils.Logging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import kotlin.text.HexFormat

class DydxV4WalletSetup @Inject constructor(
    context: Context,
    val cosmosV4Client: CosmosV4ClientProtocol,
    val parser: ParserProtocol,
    override val logger: Logging,
) : DydxWalletSetup(context, logger) {

    override fun sign(request: WalletRequest, connectedWallet: WalletInfo, signTypedDataAction: String, signTypedDataDomainName: String) {
        if (request.wallet?.id == "phantom-wallet") {
            signSolana(
                request = request,
                connectedWallet = connectedWallet,
                signTypedDataAction = signTypedDataAction,
                signTypedDataDomainName = signTypedDataDomainName,
            )
        } else {
            signEVM(
                request = request,
                connectedWallet = connectedWallet,
                signTypedDataAction = signTypedDataAction,
                signTypedDataDomainName = signTypedDataDomainName,
            )
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun signSolana(
        request: WalletRequest,
        connectedWallet: WalletInfo,
        signTypedDataAction: String,
        signTypedDataDomainName: String,
    ) {
        val address = request.address
        if (address == null) {
            _status.value = Status.createError(message = "Request address is null")
            return
        }
        val message = """
{"domain":{"name":"$signTypedDataDomainName"},"message":{"action":"$signTypedDataAction"},"primaryType":"dYdX","types":{"dYdX":[{"name":"action","type":"string"}]}}
        """.trimIndent()
        provider.signMessage(
            request = request,
            message = message,
            connected = { info ->
                if (info == null) {
                    _status.value = Status.createError(message = "Wallet not connected")
                }
            },
            status = { requireAppSwitching ->
                if (requireAppSwitching) {
                    _status.value =
                        Status.InProgress(showSwitchWalletName = connectedWallet.peerName)
                }
            },
        ) { signed, error ->
            if (signed != null && error == null) {
                // The signature from Solana is Base58 encoded
                val signedMessage: String
                try {
                    val decoded = signed.decodeBase58()
                    if (decoded.size != 64) {
                        _status.value = Status.createError(message = "Invalid signature size")
                        return@signMessage
                    }
                    // Pad a leading zero to "decoded" to make it 65 bytes before passing it down v4-client
                    val padded = byteArrayOf(0x00) + decoded
                    signedMessage = padded.toHexString(format = HexFormat.Default)
                } catch (e: Exception) {
                    _status.value = Status.createError(message = "Failed to decode Base58, ${e.message}")
                    return@signMessage
                }
                generatePrivateKey(wallet = request.wallet, privateKeySignature = signedMessage, address = address)
            } else if (error != null) {
                _status.value = Status.Error(error)
            }
        }
    }

    private fun signEVM(
        request: WalletRequest,
        connectedWallet: WalletInfo,
        signTypedDataAction: String,
        signTypedDataDomainName: String,
    ) {
        val address = request.address
        if (address == null) {
            _status.value = Status.createError(message = "Request address is null")
            return
        }

        val typedDataProvider = typedData(
            action = signTypedDataAction,
            chainId = parser.asInt(request.chainId),
            signTypedDataDomainName = signTypedDataDomainName,
        )
        provider.sign(
            request = request,
            typedDataProvider = typedDataProvider,
            status = { requireAppSwitching ->
                if (requireAppSwitching) {
                    _status.value =
                        Status.InProgress(showSwitchWalletName = connectedWallet.peerName)
                }
            },
            connected = null,
        ) { signed, error ->
            if (signed != null && error == null) {
                generatePrivateKey(request.wallet, signed, address)
            } else if (error != null) {
                if (provider.walletStatus?.connectedWallet?.peerName == "MetaMask Wallet" &&
                    error.message == "User rejected."
                ) {
                    // MetaMask wallet will send a "User rejected" response when switching chain... let's catch it and resend
                    provider.sign(
                        request = request,
                        typedDataProvider = typedDataProvider,
                        status = { requireAppSwitching ->
                            if (requireAppSwitching) {
                                _status.value =
                                    Status.InProgress(showSwitchWalletName = connectedWallet.peerName)
                            }
                        },
                        connected = null,
                    ) { signed, error ->
                        if (signed != null && error == null) {
                            generatePrivateKey(request.wallet, signed, address)
                        } else if (error != null) {
                            _status.value = Status.Error(error)
                        }
                    }
                } else {
                    _status.value = Status.Error(error)
                }
            }
            provider.disconnect()
        }
    }

    private fun generatePrivateKey(wallet: Wallet?, privateKeySignature: String, address: String) {
        cosmosV4Client.deriveCosmosKey(signature = privateKeySignature) { data ->
            if (data != null) {
                val json = Json.parseToJsonElement(data)
                val map = json.jsonObject.toMap()
                val mnemonic = parser.asString(map["mnemonic"])
                val cosmosAddress = parser.asString(map["address"])
                if (mnemonic != null) {
                    _status.value = Status.Signed(
                        SetupResult(
                            ethereumAddress = address,
                            walletId = wallet?.id,
                            cosmosAddress = cosmosAddress,
                            dydxMnemonic = mnemonic,
                        ),
                    )
                } else {
                    _status.value = Status.createError(message = "deriveCosmosKey failed")
                }
            }
        }
    }

    private fun typedData(action: String, chainId: Int?, signTypedDataDomainName: String): EIP712DomainTypedDataProvider {
        val chainId = chainId ?: 1
        val dydxSign =
            EIP712DomainTypedDataProvider(name = signTypedDataDomainName, chainId = chainId)
        dydxSign.message = message(action, chainId)
        return dydxSign
    }

    private fun message(action: String, chainId: Int): WalletTypedData {
        val definitions = mutableListOf<Map<String, String>>()
        val data = mutableMapOf<String, Any>()
        definitions.add(type(name = "action", type = "string"))
        data["action"] = action

        val message = WalletTypedData(typeName = "dYdX")
        message.definitions = definitions
        message.data = data
        return message
    }

    private fun type(name: String, type: String): Map<String, String> {
        return mapOf("name" to name, "type" to type)
    }
}
