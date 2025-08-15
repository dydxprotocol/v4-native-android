package exchange.dydx.feature.onboarding.turnkey

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxCartera.solana.Context
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.R
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import

@HiltViewModel
class DydxTurnkeyAuthViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    @ApplicationContext private val appContext: android.content.Context,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTurnkeyAuthView.ViewState?> = abacusStateManager.state.marketSummary
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(marketSummary: PerpetualMarketSummary?): DydxTurnkeyAuthView.ViewState {

        val initialProperties: Map<String, Any> = mapOf(
            // From https://console.cloud.google.com/auth/clients?inv=1&invt=Ab1olg&project=dydx-v4
            "googleClientId" to "441463123744-a02e7s84okic2ggqgdo7e7hlgpvkj3p8.apps.googleusercontent.com",
            "appScheme" to appContext.getString(R.string.app_scheme),
            "turnkeyUrl" to "https://api.turnkey.com",
            // From Turnkey console
            "turnkeyOrgId" to "3174ac51-1637-47d8-9456-19549963e2ed",
            // Indexer backend
            "backendApiUrl" to "http://dev2-indexer-apne1-lb-public-2076363889.ap-northeast-1.elb.amazonaws.com",
            "theme" to "dark"
        )


        val volume = formatter.dollarVolume(marketSummary?.volume24HUSDC)
        return DydxTurnkeyAuthView.ViewState(
            localizer = localizer,
            text = volume,
        )
    }
}
