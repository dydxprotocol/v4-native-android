package exchange.dydx.trading.feature.receipt.components.rewards

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TradeInputSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.nativeTokenLogoUrl
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.streams.ReceiptStreaming
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptRewardsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    val receiptStream: ReceiptStreaming,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptRewardsView.ViewState?> =
        receiptStream.tradeSummaryFlow
            .map {
                createViewState(it?.first)
            }
            .distinctUntilChanged()

    private fun createViewState(tradeSummary: TradeInputSummary?): DydxReceiptRewardsView.ViewState {
        val reward = tradeSummary?.reward ?: 0.0
        val text = formatter.localFormatted(reward, 6)
        return DydxReceiptRewardsView.ViewState(
            localizer = localizer,
            nativeTokenLogoUrl = abacusStateManager.nativeTokenLogoUrl,
            rewards = if (tradeSummary?.reward != null) {
                SignedAmountView.ViewState(
                    text = text ?: "",
                    sign = if (reward > 0.0) {
                        PlatformUISign.Plus
                    } else if (reward < 0.0) {
                        PlatformUISign.Minus
                    } else {
                        PlatformUISign.None
                    },
                    coloringOption = SignedAmountView.ColoringOption.SignOnly,
                )
            } else {
                null
            },
        )
    }
}
