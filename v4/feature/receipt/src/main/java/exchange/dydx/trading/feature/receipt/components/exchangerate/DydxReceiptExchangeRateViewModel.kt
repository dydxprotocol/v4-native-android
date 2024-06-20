package exchange.dydx.trading.feature.receipt.components.exchangerate

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

@HiltViewModel
class DydxReceiptExchangeRateViewModel @Inject constructor(
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
            title = localizer.localize("APP.DEPOSIT_MODAL.EXCHANGE_RATE"),
            value = createExchangeRateString(transferInput),
        )
    }

    private fun createExchangeRateString(
        transferInput: TransferInput?
    ): String? {
        val exchangeRate = transferInput?.summary?.exchangeRate ?: return null
        val type = transferInput.type ?: return null
        val token = transferInput.token ?: return null
        val symbol = transferInput.resources?.tokenResources?.get(token)?.symbol ?: return null
        when (type) {
            TransferType.deposit -> {
                val converted = formatter.raw(exchangeRate, 2) ?: return null
                return "1 $symbol = $converted USDC"
            }
            TransferType.withdrawal -> {
                if (exchangeRate > 0) {
                    val converted = formatter.raw(1 / exchangeRate, 2) ?: return null
                    return "$converted USDC = 1 $symbol"
                } else {
                    return null
                }
            }
            else -> return null
        }
    }
}
