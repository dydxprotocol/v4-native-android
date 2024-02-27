package exchange.dydx.newsalerts

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.feature.shared.views.SelectionBar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DydxNewsAlertsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
) : ViewModel(), DydxViewModel {

    private val _state: MutableStateFlow<DydxNewsAlertsView.ViewState?> = MutableStateFlow(createViewState())

    val state: Flow<DydxNewsAlertsView.ViewState?> = _state

    private fun createViewState(): DydxNewsAlertsView.ViewState {
        return DydxNewsAlertsView.ViewState(
            localizer = localizer,
            selectionBarViewState = SelectionBar.ViewState(
                localizer = localizer,
                items = listOf(
                    SelectionBar.Item(
                        text = localizer.localize("APP.GENERAL.ALERTS"),
                    ),
                    SelectionBar.Item(
                        text = localizer.localize("APP.GENERAL.NEWS"),
                    ),
                ),
                currentSelection = 0,
                onSelectionChanged = {
                    _state.value = _state.value?.copy(
                        selectionBarViewState = _state.value?.selectionBarViewState?.copy(
                            currentSelection = it,
                        ),
                    )
                },
            ),
        )
    }
}
