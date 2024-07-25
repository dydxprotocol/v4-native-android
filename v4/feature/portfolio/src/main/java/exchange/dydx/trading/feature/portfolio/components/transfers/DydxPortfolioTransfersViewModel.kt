package exchange.dydx.trading.feature.portfolio.components.transfers

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.output.account.TransferRecordType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.IntervalText
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioTransfersViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioTransfersView.ViewState?> =
        abacusStateManager.state.transfers
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        transfers: List<SubaccountTransfer>?,
    ): DydxPortfolioTransfersView.ViewState {
        return DydxPortfolioTransfersView.ViewState(
            localizer = localizer,
            transfers = transfers?.mapNotNull { transfer ->
                DydxPortfolioTransfersItemView.ViewState(
                    localizer = localizer,
                    type = when (transfer.type) {
                        TransferRecordType.DEPOSIT -> DydxPortfolioTransfersItemView.TransferType.Deposit
                        TransferRecordType.WITHDRAW -> DydxPortfolioTransfersItemView.TransferType.Withdrawal
                        TransferRecordType.TRANSFER_IN -> DydxPortfolioTransfersItemView.TransferType.TransferIn
                        TransferRecordType.TRANSFER_OUT -> DydxPortfolioTransfersItemView.TransferType.TransferOut
                    },
                    amount = transfer.amount?.let {
                        SignedAmountView.ViewState(
                            text = formatter.dollarVolume(it, 2),
                            coloringOption = SignedAmountView.ColoringOption.SignOnly,
                            sign = when (transfer.type) {
                                TransferRecordType.DEPOSIT, TransferRecordType.TRANSFER_IN -> PlatformUISign.Plus
                                TransferRecordType.WITHDRAW, TransferRecordType.TRANSFER_OUT -> PlatformUISign.Minus
                                else -> PlatformUISign.None
                            },
                        )
                    },
                    address = when (transfer.type) {
                        TransferRecordType.DEPOSIT, TransferRecordType.TRANSFER_IN ->
                            localizer.localizeWithParams(
                                path = "APP.GENERAL.FROM",
                                params = mapOf(
                                    "FROM" to (getTruncatedAddress(transfer.fromAddress ?: "-")),
                                ),
                            )
                        TransferRecordType.WITHDRAW, TransferRecordType.TRANSFER_OUT ->
                            localizer.localizeWithParams(
                                path = "APP.GENERAL.TO",
                                params = mapOf(
                                    "TO" to (getTruncatedAddress(transfer.toAddress ?: "-")),
                                ),
                            )
                    },
                    date = transfer.updatedAtMilliseconds.let {
                        IntervalText.ViewState(
                            date = Instant.ofEpochMilli(it.toLong()),
                        )
                    },
                )
            } ?: emptyList(),
        )
    }

    private fun getTruncatedAddress(address: String): String {
        return if (address.length <= 10) {
            address
        } else {
            address.take(6) + "..." + address.takeLast(4)
        }
    }
}
