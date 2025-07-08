package exchange.dydx.trading.feature.vault.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultHistoryEntry
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.math.max

@HiltViewModel
class DydxVaultInfoViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val selectedChartEntryFlow: Flow<VaultHistoryEntry?>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultInfoView.ViewState?> =
        combine(
            abacusStateManager.state.vault,
            selectedChartEntryFlow,
        ) { vault, selectedChartEntry ->
            createViewState(vault, selectedChartEntry)
        }
            .distinctUntilChanged()

    private fun createViewState(
        vault: Vault?,
        selectedChartEntry: VaultHistoryEntry?,
    ): DydxVaultInfoView.ViewState {
        val pnl = SignedAmountView.ViewState.fromDouble(vault?.account?.allTimeReturnUsdc) {
            formatter.dollar(it?.absoluteValue, 2) ?: "-"
        }
        val apr = if (vault?.details != null) {
            val betterReturnPercent = max(vault.details?.thirtyDayReturnPercent ?: 0.0, vault.details?.ninetyDayReturnPercent ?: 0.0)
            SignedAmountView.ViewState.fromDouble(betterReturnPercent) {
                formatter.percent(it?.absoluteValue, 0) ?: "-"
            }
        } else {
            null
        }
        return DydxVaultInfoView.ViewState(
            localizer = localizer,
            balance = formatter.dollar(vault?.account?.balanceUsdc, digits = 2),
            pnl = pnl,
            apr = apr,
            tvl = formatter.dollar(vault?.details?.totalValue, digits = 0),
            chartEntrySelected = selectedChartEntry != null,
        )
    }
}
