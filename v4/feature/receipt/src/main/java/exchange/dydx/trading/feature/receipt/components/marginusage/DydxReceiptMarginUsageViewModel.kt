package exchange.dydx.trading.feature.receipt.components.marginusage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.MarginUsageView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptMarginUsageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptMarginUsageView.ViewState?> =
        abacusStateManager.state.selectedSubaccount
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        subaccount: Subaccount?
    ): DydxReceiptMarginUsageView.ViewState {
        return DydxReceiptMarginUsageView.ViewState(
            localizer = localizer,
            formatter = formatter,
            before = if (subaccount?.marginUsage?.current != null) {
                MarginUsageView.ViewState(
                    localizer = localizer,
                    displayOption = MarginUsageView.DisplayOption.IconAndValue,
                    percent = subaccount.marginUsage?.current ?: 0.0,
                )
            } else {
                null
            },
            after = if (subaccount?.marginUsage?.postOrder != null) {
                MarginUsageView.ViewState(
                    localizer = localizer,
                    displayOption = MarginUsageView.DisplayOption.IconAndValue,
                    percent = subaccount.marginUsage?.postOrder ?: 0.0,
                )
            } else {
                null
            },
        )
    }
}
