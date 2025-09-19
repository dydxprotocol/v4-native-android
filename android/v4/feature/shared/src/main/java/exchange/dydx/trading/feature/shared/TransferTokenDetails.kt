package exchange.dydx.trading.feature.shared

import android.R.attr.path
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.localizeWithParams
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.featureflags.RemoteFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.toMutableList

@Singleton
class TransferTokenDetails @Inject constructor(
    private val abacusStateManager: AbacusStateManagerProtocol,
    @CoroutineScopes.App private val scope: CoroutineScope,
) {
    val selectedToken = MutableStateFlow<TransferTokenInfo?>(null)

    val defaultToken = MutableStateFlow<TransferTokenInfo?>(null)

    private val _refreshCounter = MutableStateFlow(0)
    val refreshCounter: StateFlow<Int> = _refreshCounter.asStateFlow()

    private val _infos = MutableStateFlow<List<TransferTokenInfo>>(emptyList())

    val marketPrices: Flow<Map<String, Double>> = abacusStateManager.state.marketMap
        .map { marketMap ->
            listOf("ETH-USD", "POL-USD", "SOL-USD", "AVAX-USD")
                .mapNotNull { id ->
                    marketMap?.get(id)?.oraclePrice?.toDouble()?.let { id to it }
                }.toMap()
        }
        .distinctUntilChanged()
        .shareIn(scope = scope, started = SharingStarted.Lazily, replay = 1)

    val infos: StateFlow<List<TransferTokenInfo>> = combine(
        _infos,
        marketPrices,
    ) { infos, marketPrices ->
        infos.map { token ->
            var updatedToken = token

            if (token.token == TransferToken.USDC && token.amount == null) {
                updatedToken = updatedToken.copy(amount = token.usdcAmount)
            } else if (token.amount != null && token.amount!! > 0) {
                val key = "${token.token}-USD"
                marketPrices[key]?.let { price ->
                    updatedToken = updatedToken.copy(usdcAmount = token.amount!! * price)
                }
            }
            updatedToken
        }.sortedByDescending { it.usdcAmount ?: 0.0 }
    }.distinctUntilChanged()
        .stateIn(scope = scope, started = SharingStarted.Lazily, emptyList())

    init {
        _infos.value = mainnetTokens // Replace with condition if needed
    }

    fun update(info: TransferTokenInfo) {
        val updatedList = _infos.value.toMutableList()
        val index = updatedList.indexOfFirst {
            it.chainId == info.chainId && it.tokenAddress == info.tokenAddress
        }

        if (index >= 0) {
            updatedList[index] = info
            _infos.value = updatedList

            if (selectedToken.value?.chain == info.chain &&
                selectedToken.value?.tokenAddress == info.tokenAddress
            ) {
                selectedToken.value = info
            }

            if (defaultToken.value?.chain == info.chain &&
                defaultToken.value?.tokenAddress == info.tokenAddress
            ) {
                defaultToken.value = info
            }
        } else {
            error("Could not find token info to update")
        }
    }

    fun refresh() {
        _refreshCounter.value += 1
    }
}

enum class TransferChain {
    Ethereum, Optimism, Arbitrum, Base, Polygon, Solana, Avalanche;

    val supportedDepositTokenString: String
        get() = when (this) {
            Ethereum -> "ETH, USDC"
            Optimism -> "ETH, USDC"
            Arbitrum -> "ETH, USDC"
            Base -> "ETH, USDC"
            Polygon -> "POL, USDC"
            Solana -> "USDC"
            Avalanche -> "USDC"
        }

    fun depositFeesString(localizer: LocalizerProtocol): String {
        return when (this) {
            Ethereum -> localizer.localizeWithParams(
                path = "APP.DEPOSIT_MODAL.FREE_ABOVE",
                params = mapOf("AMOUNT" to "$100"),
            )

            else -> localizer.localize(path = "APP.GENERAL.FREE")
        }
    }

    fun depositWarningString(localizer: LocalizerProtocol, remoteFlags: RemoteFlags): String {
        val tokens = when (this) {
            Ethereum, Optimism, Arbitrum, Base -> "ETH " + localizer.localize(path = "APP.GENERAL.OR") + " USDC"
            Polygon -> "POL " + localizer.localize(path = "APP.GENERAL.OR") + " USDC"
            Solana -> "USDC"
            Avalanche -> " USDC"
        }

        val minSlowVal = if (this == TransferChain.Ethereum) {
            remoteFlags.getParamStoreValue("eth_min_slow", "-")
        } else {
            remoteFlags.getParamStoreValue("default_min_slow", "-")
        }
        val minFastVal = if (this == TransferChain.Ethereum) {
            remoteFlags.getParamStoreValue("eth_min_fast", "-")
        } else {
            remoteFlags.getParamStoreValue("default_min_fast", "-")
        }
        val maxVal = if (this == TransferChain.Ethereum) {
            remoteFlags.getParamStoreValue("eth_max", "-")
        } else {
            remoteFlags.getParamStoreValue("default_max", "-")
        }

        return localizer.localizeWithParams(
            path = "APP.TURNKEY_ONBOARD.DEPOSIT_NETWORK_WARNING",
            params = mapOf(
                "ASSETS" to tokens,
                "NETWORK" to name,
                "MIN_DEPOSIT" to minSlowVal,
                "MIN_INSTANT_DEPOSIT" to minFastVal,
                "MAX_DEPOSIT" to maxVal,
            ),
        )
    }

    fun chainLogoUrl(deploymentUri: String): String {
        val logoName = when (this) {
            Ethereum -> "ethereum.png"
            Optimism -> "optimism.png"
            Arbitrum -> "arbitrum.png"
            Base -> "base.png"
            Polygon -> "polygon.png"
            Solana -> "solana.png"
            Avalanche -> "avalanche.png"
        }
        return "$deploymentUri/chains/$logoName"
    }

    companion object {
        fun fromString(chainName: String): TransferChain? {
            return TransferChain.entries.find { it.name.equals(chainName, ignoreCase = true) }
        }
    }
}

enum class TransferToken {
    ETH, USDC, POL, SOL, AVAX
}

data class TransferTokenInfo(
    val chain: TransferChain,
    val chainId: String,
    val token: TransferToken,
    val tokenAddress: String,
    var amount: Double? = null,
    var usdcAmount: Double? = null
) {
    fun chainLogUrl(deploymentUri: String): String {
        val logoName = when (chain) {
            TransferChain.Ethereum -> "ethereum.png"
            TransferChain.Optimism -> "optimism.png"
            TransferChain.Arbitrum -> "arbitrum.png"
            TransferChain.Base -> "base.png"
            TransferChain.Polygon -> "polygon.png"
            TransferChain.Solana -> "solana.png"
            TransferChain.Avalanche -> "avalanche.png"
        }
        return "$deploymentUri/chains/$logoName"
    }

    fun tokenLogoUrl(deploymentUri: String): String {
        val logoName = when (token) {
            TransferToken.ETH -> "eth.png"
            TransferToken.USDC -> "usdc.png"
            TransferToken.POL -> "pol.png"
            TransferToken.SOL -> "sol.png"
            TransferToken.AVAX -> "avax.png"
        }
        return "$deploymentUri/currencies/$logoName"
    }

    val decimals: Int
        get() = when (token) {
            TransferToken.ETH, TransferToken.POL, TransferToken.AVAX -> 18
            TransferToken.USDC -> 6
            TransferToken.SOL -> 9
        }
}

private val mainnetTokens = listOf(
    TransferTokenInfo(TransferChain.Ethereum, "1", TransferToken.USDC, "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"),
    TransferTokenInfo(TransferChain.Base, "8453", TransferToken.USDC, "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913"),
    TransferTokenInfo(TransferChain.Optimism, "10", TransferToken.USDC, "0x0b2c639c533813f4aa9d7837caf62653d097ff85"),
    TransferTokenInfo(TransferChain.Arbitrum, "42161", TransferToken.USDC, "0xaf88d065e77c8cC2239327C5EDb3A432268e5831"),
    TransferTokenInfo(TransferChain.Polygon, "137", TransferToken.USDC, "0x3c499c542cef5e3811e1192ce70d8cc03d5c3359"),
    TransferTokenInfo(TransferChain.Avalanche, "43114", TransferToken.USDC, "0xB97EF9Ef8734C71904D8002F8b6Bc66Dd9c48a6E"),

    TransferTokenInfo(TransferChain.Ethereum, "1", TransferToken.ETH, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
    TransferTokenInfo(TransferChain.Base, "8453", TransferToken.ETH, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
    TransferTokenInfo(TransferChain.Optimism, "10", TransferToken.ETH, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
    TransferTokenInfo(TransferChain.Arbitrum, "42161", TransferToken.ETH, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
    TransferTokenInfo(TransferChain.Polygon, "137", TransferToken.POL, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
    TransferTokenInfo(TransferChain.Avalanche, "43114", TransferToken.AVAX, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),

    // TransferTokenInfo(TransferChain.Solana, "solana", TransferToken.SOL, "solana-native"),
    TransferTokenInfo(TransferChain.Solana, "solana", TransferToken.USDC, "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"),
)

private val testnetTokens = listOf(
    TransferTokenInfo(TransferChain.Ethereum, "11155111", TransferToken.USDC, "0x482ff112ae0658a014978f53120a64e111e6bedf"),
    TransferTokenInfo(TransferChain.Base, "84532", TransferToken.USDC, "0x0F2559677a6CF88b48BBFAddE1757D4f302C8e23"),
    TransferTokenInfo(TransferChain.Optimism, "11155420", TransferToken.USDC, "0xD0C591da9805D1f801B297bDF46352287E0A6A63"),
    TransferTokenInfo(TransferChain.Arbitrum, "421614", TransferToken.USDC, "0x75faf114eafb1BDbe2F0316DF893fd58CE46AA4d"),
    TransferTokenInfo(TransferChain.Polygon, "80002", TransferToken.USDC, "0x41E94Eb019C0762f9Bfcf9Fb1E58725BfB0e7582"),
    TransferTokenInfo(TransferChain.Avalanche, "43113", TransferToken.USDC, "0x5425890298aed601595a70AB815c96711a31Bc65"),

    TransferTokenInfo(TransferChain.Ethereum, "11155111", TransferToken.ETH, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
    TransferTokenInfo(TransferChain.Base, "84532", TransferToken.ETH, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
    TransferTokenInfo(TransferChain.Optimism, "11155420", TransferToken.ETH, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
    TransferTokenInfo(TransferChain.Arbitrum, "421614", TransferToken.ETH, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
    TransferTokenInfo(TransferChain.Polygon, "80002", TransferToken.POL, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
    TransferTokenInfo(TransferChain.Avalanche, "43113", TransferToken.AVAX, "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),

    // TransferTokenInfo(TransferChain.Solana, "solana-devnet", TransferToken.SOL, "solana-devnet-native"),
    TransferTokenInfo(TransferChain.Solana, "solana-devnet", TransferToken.USDC, "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU"),
)
