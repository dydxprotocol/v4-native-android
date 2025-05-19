package exchange.dydx.newsalerts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.navigation.DydxAnimation
import exchange.dydx.trading.feature.newsalerts.alerts.DydxAlertsView
import exchange.dydx.trading.feature.newsalerts.news.DydxNewsView
import exchange.dydx.trading.feature.shared.views.SelectionBar

@Preview
@Composable
fun Preview_DydxNewsAlertsView() {
    DydxThemedPreviewSurface {
        DydxNewsAlertsView.Content(Modifier, DydxNewsAlertsView.ViewState.preview)
    }
}

object DydxNewsAlertsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val selectionBarViewState: SelectionBar.ViewState?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                selectionBarViewState = SelectionBar.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxNewsAlertsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state?.selectionBarViewState == null) {
            return
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            Column {
                SelectionBar.Content(
                    modifier = modifier.fillMaxWidth(),
                    state = state.selectionBarViewState,
                )

                DydxAnimation.AnimateFadeInOut(
                    visible = state.selectionBarViewState.currentSelection == 0,
                ) {
                    DydxAlertsView.Content(Modifier)
                }

                DydxAnimation.AnimateFadeInOut(
                    visible = state.selectionBarViewState.currentSelection == 1,
                ) {
                    DydxNewsView.Content(Modifier)
                }
            }
        }
    }
}
