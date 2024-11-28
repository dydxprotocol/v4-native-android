package exchange.dydx.dydxCartera.v4

import android.content.Context
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.cartera.entities.Wallet
import exchange.dydx.cartera.typeddata.EIP712DomainTypedDataProvider
import exchange.dydx.cartera.typeddata.WalletTypedData
import exchange.dydx.cartera.walletprovider.WalletInfo
import exchange.dydx.cartera.walletprovider.WalletRequest
import exchange.dydx.dydxCartera.DydxWalletSetup
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientProtocol
import exchange.dydx.utilities.utils.Logging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

class DydxV4WalletSetup @Inject constructor(
    context: Context,
    val cosmosV4Client: CosmosV4ClientProtocol,
    val parser: ParserProtocol,
    override val logger: Logging,
) : DydxWalletSetup(context, logger) {

    override fun sign(request: WalletRequest, connectedWallet: WalletInfo, signTypedDataAction: String, signTypedDataDomainName: String) {
        val address = request.address
        if (address == null) {
            _status.value = Status.createError(message = "Request address is null")
            return
        }
        provider.sign(
            request = request,
            typedDataProvider = typedData(
                action = signTypedDataAction,
                chainId = parser.asInt(request.chainId),
                signTypedDataDomainName = signTypedDataDomainName,
            ),
            status = { requireAppSwitching ->
                if (requireAppSwitching) {
                    _status.value = Status.InProgress(showSwitchWalletName = connectedWallet.peerName)
                }
            },
            connected = null,
        ) { signed, error ->
            if (signed != null && error == null) {
                generatePrivateKey(request.wallet, signed, address)
            } else if (error != null) {
                _status.value = Status.Error(error)
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
                            mnemonic = mnemonic,
                            apiKey = null,
                            secret = null,
                            passPhrase = null,
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
        val dydxSign = EIP712DomainTypedDataProvider(name = signTypedDataDomainName, chainId = chainId)
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
