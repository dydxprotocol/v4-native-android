package exchange.dydx.trading.feature.receipt.components.slippage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.components.DydxReceiptItemView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptSlippageViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptItemView.ViewState?> =
        abacusStateManager.state.transferInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        transferInput: TransferInput?
    ): DydxReceiptItemView.ViewState {
        return DydxReceiptItemView.ViewState(
            localizer = localizer,
            title = localizer.localize("APP.DEPOSIT_MODAL.SLIPPAGE"),
            value = createTransferSlippageString(transferInput),
        )
    }

    private fun createTransferSlippageString(
        transferInput: TransferInput?
    ): String? {
        val transferDuration = transferInput?.summary?.slippage ?: return null
        return formatter.percent(transferDuration / 100, 2)
    }
}
