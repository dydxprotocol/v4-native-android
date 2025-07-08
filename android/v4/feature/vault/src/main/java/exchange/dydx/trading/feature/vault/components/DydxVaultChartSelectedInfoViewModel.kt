package exchange.dydx.trading.feature.vault.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultHistoryEntry
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class DydxVaultChartSelectedInfoViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val formatter: DydxFormatter,
    private val selectedChartEntryFlow: Flow<VaultHistoryEntry?>,
    private val vaultHistoryFlow: Flow<@JvmSuppressWildcards List<VaultHistoryEntry>?>,
    private val chartTypeFlow: Flow<@JvmSuppressWildcards ChartType?>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultChartSelectedInfoView.ViewState?> =
        combine(
            vaultHistoryFlow,
            chartTypeFlow.filterNotNull(),
            selectedChartEntryFlow,
        ) { history, chartType, selectedChartEntry ->
            createViewState(history, chartType, selectedChartEntry)
        }
            .distinctUntilChanged()

    private fun createViewState(
        history: List<VaultHistoryEntry>?,
        chartType: ChartType,
        selectedChartEntry: VaultHistoryEntry?
    ): DydxVaultChartSelectedInfoView.ViewState {
        val (value, percent) = when (chartType) {
            ChartType.EQUITY -> createDiffs(
                history?.firstOrNull()?.equity,
                selectedChartEntry?.equity,
            )

            ChartType.PNL -> createDiffs(
                history?.firstOrNull()?.totalPnl,
                selectedChartEntry?.totalPnl,
            )
        }
        val curValue = when (chartType) {
            ChartType.EQUITY -> selectedChartEntry?.equity
            ChartType.PNL -> selectedChartEntry?.totalPnl
        }
        return DydxVaultChartSelectedInfoView.ViewState(
            localizer = localizer,
            currentValue = formatter.dollar(curValue, digits = 2),
            entryDate = formatter.dateTime(selectedChartEntry?.dateInstance),
            change = SignedAmountView.ViewState.fromDouble(value) {
                if (it == 0.0) {
                    ""
                } else {
                    val dollarValue = formatter.dollar(value, digits = 2) ?: "-"
                    val percentValue = formatter.percent(percent, 2) ?: "-"
                    "$dollarValue ($percentValue)"
                }
            },
        )
    }

    private fun createDiffs(first: Double?, current: Double?): Pair<Double, Double> {
        if (first == null || current == null) {
            return Pair(0.0, 0.0)
        }
        val percent = if (first != 0.0) {
            (current - first) / first
        } else {
            0.0
        }
        return Pair(current - first, percent)
    }
}
