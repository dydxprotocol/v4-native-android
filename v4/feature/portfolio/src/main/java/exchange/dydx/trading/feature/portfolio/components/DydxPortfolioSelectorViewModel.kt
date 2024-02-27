package exchange.dydx.trading.feature.portfolio.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.portfolio.DydxPortfolioView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioSelectorViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val displayContentFlow: MutableStateFlow<DydxPortfolioView.DisplayContent>,
) : ViewModel(), DydxViewModel {
    val state: Flow<DydxPortfolioSelectorView.ViewState?> = displayContentFlow
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(displayContent: DydxPortfolioView.DisplayContent): DydxPortfolioSelectorView.ViewState? {
        return DydxPortfolioSelectorView.ViewState(
            localizer = localizer,
            currentContent = displayContent,
            onSelectionChanged = {
                displayContentFlow.value = it
            },
        )
    }
}
