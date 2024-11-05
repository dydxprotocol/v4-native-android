package exchange.dydx.trading.feature.receipt.components.fee

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxReceiptTransferFeeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxReceiptBaseFeeView.ViewState?> =
        abacusStateManager.state.transferInput
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        transferInput: TransferInput?
    ): DydxReceiptBaseFeeView.ViewState {
        val gasFee = transferInput?.summary?.gasFee
        return DydxReceiptBaseFeeView.ViewState(
            localizer = localizer,
            feeType = "Gas",
            feeFont = if (gasFee == null) {
                null
            } else if (gasFee > 0.0) {
                DydxReceiptBaseFeeView.FeeFont.Number(
                    formatter.dollar(gasFee, 2) ?: "",
                )
            } else {
                DydxReceiptBaseFeeView.FeeFont.String(localizer.localize("APP.GENERAL.FREE"))
            },
        )
    }
}
