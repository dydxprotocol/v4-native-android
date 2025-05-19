package exchange.dydx.trading.feature.profile.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_DydxHelpView() {
    DydxThemedPreviewSurface {
        DydxHelpView.Content(Modifier, DydxHelpView.ViewState.preview)
    }
}

object DydxHelpView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val items: List<DydxHelpItemView.ViewState> = emptyList(),
        val backAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                items = listOf(
                    DydxHelpItemView.ViewState.preview,
                    DydxHelpItemView.ViewState.preview,
                    DydxHelpItemView.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxHelpViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.HEADER.HELP"),
                backAction = state.backAction,
            )

            state.items.forEachIndexed { index, item ->
                DydxHelpItemView.Content(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ThemeShapes.HorizontalPadding),
                    state = item,
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
