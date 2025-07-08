package exchange.dydx.trading.feature.receipt.components.exchangereceived

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.components.DydxReceiptItemView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class DydxReceiptExchangeReceivedViewModel @Inject constructor(
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
            title = localizer.localize("APP.DEPOSIT_MODAL.EXCHANGE_RECEIVED"),
            value = createExchangeReceivedString(transferInput),
        )
    }

    private fun createExchangeReceivedString(
        transferInput: TransferInput?
    ): String? {
        val exchangeRate = transferInput?.summary?.exchangeRate ?: return null
        val type = transferInput.type ?: return null
        val token = transferInput.token ?: return null
        val symbol =
            transferInput.resources?.tokenResources?.toMap()?.get(token)?.symbol ?: return null
        when (type) {
            TransferType.deposit -> {
                val usdcSize = transferInput.summary?.usdcSize ?: return null
                val converted = formatter.raw(usdcSize, 2) ?: return null
                return "$converted USDC"
            }

            TransferType.withdrawal -> {
                val toAmount = transferInput.summary?.toAmount
                if (toAmount != null) {
                    val decimals = transferInput.resources?.tokenResources?.toMap()?.get(token)?.decimals ?: return null
                    val size = toAmount.toDouble() / 10.0.pow(decimals)
                    val converted = formatter.raw(size, 4) ?: return null
                    return "$converted $symbol"
                } else {
                    val usdcSize = transferInput.summary?.usdcSize ?: return null
                    if (exchangeRate > 0) {
                        val converted = formatter.raw(usdcSize * exchangeRate, 2) ?: return null
                        return "$converted $symbol"
                    } else {
                        return null
                    }
                }
            }

            else -> return null
        }
    }
}
