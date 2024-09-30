package exchange.dydx.trading.feature.receipt.components.transferduration

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.receipt.components.DydxReceiptItemView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class DydxReceiptTransferDurationViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val parser: ParserProtocol,
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
            title = localizer.localize("APP.DEPOSIT_MODAL.ESTIMATED_TIME"),
            value = createTransferDurationString(transferInput),
        )
    }

    private fun createTransferDurationString(
        transferInput: TransferInput?
    ): String? {
        val transferDuration = transferInput?.summary?.estimatedRouteDurationSeconds ?: return null
        val minutes = parser.asString(ceil(transferDuration / 60).toInt()) ?: return null

        return localizer.localizeWithParams(
            path = "APP.GENERAL.TIME_STRINGS.X_MINUTES",
            params = mapOf("X" to minutes),
        )
    }
}
