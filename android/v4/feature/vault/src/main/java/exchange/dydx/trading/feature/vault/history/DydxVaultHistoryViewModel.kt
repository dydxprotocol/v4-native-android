package exchange.dydx.trading.feature.vault.history

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultTransfer
import exchange.dydx.abacus.functional.vault.VaultTransferType
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DydxVaultHistoryViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    private val mintscanUrl = abacusStateManager.environment?.links?.mintscan

    val state: Flow<DydxVaultHistoryView.ViewState?> = abacusStateManager.state.vault
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(vault: Vault?): DydxVaultHistoryView.ViewState? {
        val history = vault?.account?.vaultTransfers ?: return null
        return DydxVaultHistoryView.ViewState(
            localizer = localizer,
            backAction = {
                router.navigateBack()
            },
            items = history.mapNotNull {
                createItemViewState(it)
            },
        )
    }

    private fun createItemViewState(item: VaultTransfer): DydxVaultHistoryItemView.ViewState? {
        val timestamp = item.timestampMs?.toLong() ?: return null
        val dateInstant = Instant.ofEpochMilli(timestamp)
        val dateFormater =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", formatter.locale)
        val timeFormater =
            DateTimeFormatter.ofPattern("h:mma", formatter.locale)
        return DydxVaultHistoryItemView.ViewState(
            localizer = localizer,
            date = formatter.dateTime(dateInstant, formatter = dateFormater),
            time = formatter.dateTime(dateInstant, formatter = timeFormater),
            action = when (item.type) {
                VaultTransferType.DEPOSIT -> localizer.localize("APP.GENERAL.DEPOSIT")
                VaultTransferType.WITHDRAWAL -> localizer.localize("APP.GENERAL.WITHDRAW")
                else -> null
            },
            amount = formatter.dollar(item.amountUsdc, digits = 2),
            onTapAction = item.transactionHash?.let {
                {
                    if (mintscanUrl != null) {
                        val url = mintscanUrl.replace("{tx_hash}", it)
                        router.navigateTo(route = url)
                    }
                }
            },
        )
    }
}
