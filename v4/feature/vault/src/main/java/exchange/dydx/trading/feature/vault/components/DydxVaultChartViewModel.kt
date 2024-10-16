package exchange.dydx.trading.feature.vault.components

import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultHistoryEntry
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.shared.views.SparklineView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DydxVaultChartViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val selectedChartEntry: MutableStateFlow<VaultHistoryEntry?>,
    private val vaultHistory: MutableStateFlow<List<VaultHistoryEntry>?>,
    private val chartType: MutableStateFlow<ChartType?>,
) : ViewModel(), DydxViewModel, OnChartValueSelectedListener {

    private val typeIndex = MutableStateFlow(0)
    private val resolutionIndex = MutableStateFlow(1)

    val state: Flow<DydxVaultChartView.ViewState?> =
        combine(
            abacusStateManager.state.vault.map {
                it?.details?.history
            }.distinctUntilChanged(),
            typeIndex,
            resolutionIndex,
        ) { history, typeIndex, resolutionIndex ->
            createViewState(history, typeIndex, resolutionIndex)
        }
            .distinctUntilChanged()

    init {
        chartType.value = ChartType.allTypes[typeIndex.value]
    }

    private fun createViewState(
        history: IList<VaultHistoryEntry>?,
        currentTypeIndex: Int,
        currentResolutionIndex: Int,
    ): DydxVaultChartView.ViewState {
        return DydxVaultChartView.ViewState(
            localizer = localizer,
            typeTitles = ChartType.allTypes.map { it.title(localizer) },
            typeIndex = currentTypeIndex,
            onTypeChanged = {
                typeIndex.value = it
                chartType.value = ChartType.allTypes[it]
            },
            resolutionTitles = ChartResolution.allResolutions.map { it.title(localizer) },
            resolutionIndex = currentResolutionIndex,
            onResolutionChanged = {
                resolutionIndex.value = it
            },
            sparkline = SparklineView.ViewState(
                sparkline = createSparkline(
                    history = history,
                    type = ChartType.allTypes[currentTypeIndex],
                    resolution = ChartResolution.allResolutions[currentResolutionIndex],
                ),
                lineWidth = 3.0,
                selectionListener = this,
            ),
        )
    }

    private fun createSparkline(
        history: IList<VaultHistoryEntry>?,
        type: ChartType,
        resolution: ChartResolution
    ): LineChartDataSet {
        val filtered = history?.filter { entry ->
            val now = Clock.System.now()
            val then = entry.dateInstance ?: return@filter false
            val diff = now.toEpochMilliseconds() - then.toEpochMilli()
            when (resolution) {
                ChartResolution.DAY -> diff <= Duration.ofDays(1).toMillis()
                ChartResolution.WEEK -> diff <= Duration.ofDays(7).toMillis()
                ChartResolution.MONTH -> diff <= Duration.ofDays(30).toMillis()
            }
        }?.reversed()
        vaultHistory.value = filtered
        if (filtered.isNullOrEmpty()) {
            return LineChartDataSet(emptyList(), type.title(localizer))
        }
        val firstDate = filtered[0].date?.toFloat() ?: 0f
        val entries = filtered.map { entry ->
            val x = (entry.date?.toFloat() ?: 0f) - firstDate
            val y = when (type) {
                ChartType.PNL -> entry.totalPnl
                ChartType.EQUITY -> entry.equity
            }?.toFloat()
            if (x == null || y == null) {
                return@map null
            }
            Entry(x, y, entry)
        }
        return LineChartDataSet(entries, type.title(localizer))
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        selectedChartEntry.value = e?.data as? VaultHistoryEntry
    }

    override fun onNothingSelected() {
        selectedChartEntry.value = null
    }
}

internal val VaultHistoryEntry.dateInstance: Instant?
    get() = date?.let { Instant.ofEpochMilli(it.toLong()) }

enum class ChartType {
    PNL,
    EQUITY;

    fun title(localizer: LocalizerProtocol): String = localizer.localize(titleKey)

    val titleKey: String
        get() = when (this) {
            PNL -> "APP.VAULTS.VAULT_PNL"
            EQUITY -> "APP.VAULTS.VAULT_EQUITY"
        }

    companion object {
        val allTypes = listOf(PNL, EQUITY)
    }
}

private enum class ChartResolution {
    DAY,
    WEEK,
    MONTH;

    fun title(localizer: LocalizerProtocol): String = localizer.localize(titleKey)

    val titleKey: String
        get() = when (this) {
            DAY -> "APP.GENERAL.TIME_STRINGS.1D"
            WEEK -> "APP.GENERAL.TIME_STRINGS.7D"
            MONTH -> "APP.GENERAL.TIME_STRINGS.30D"
        }

    companion object {
        val allResolutions = listOf(DAY, WEEK, MONTH)
    }
}
