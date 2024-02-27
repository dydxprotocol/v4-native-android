package ${PACKAGE_NAME}

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@HiltViewModel
class ${NAME}Model @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<${NAME}.ViewState?> = abacusStateManager.state.marketSummary
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(marketSummary: PerpetualMarketSummary?): ${NAME}.ViewState {
        val volume = formatter.dollarVolume(marketSummary?.volume24HUSDC)
         return ${NAME}.ViewState(
            localizer = localizer,
            text = volume,
        )
    }
}
