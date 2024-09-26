package exchange.dydx.trading.feature.vault.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxVaultChartViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    private val typeIndex = MutableStateFlow(0)
    private val resolutionIndex = MutableStateFlow(0)

    val state: Flow<DydxVaultChartView.ViewState?> =
        combine(
            typeIndex,
            resolutionIndex,
        ) { typeIndex, resolutionIndex ->
            createViewState(typeIndex, resolutionIndex)
        }
            .distinctUntilChanged()

    private fun createViewState(
        currentTypeIndex: Int,
        currentResolutionIndex: Int,
    ): DydxVaultChartView.ViewState {
        return DydxVaultChartView.ViewState(
            localizer = localizer,
            typeTitles = ChartType.allTypes.map { it.title(localizer) },
            typeIndex = currentTypeIndex,
            onTypeChanged = {
                typeIndex.value = it
            },
            resolutionTitles = ChartResolution.allResolutions.map { it.title(localizer) },
            resolutionIndex = currentResolutionIndex,
            onResolutionChanged = {
                resolutionIndex.value = it
            },
        )
    }
}

private enum class ChartType {
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
