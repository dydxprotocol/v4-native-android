package exchange.dydx.trading.feature.transfer.noble

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTransferNobleAddressViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTransferNobleAddressView.ViewState?> =
        abacusStateManager.state.marketSummary
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(marketSummary: PerpetualMarketSummary?): DydxTransferNobleAddressView.ViewState {
        val volume = formatter.dollarVolume(marketSummary?.volume24HUSDC)
        return DydxTransferNobleAddressView.ViewState(
            localizer = localizer,
            text = volume,
        )
    }
}
