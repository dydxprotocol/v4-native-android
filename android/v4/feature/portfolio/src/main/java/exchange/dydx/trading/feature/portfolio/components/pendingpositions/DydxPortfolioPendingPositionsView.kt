package exchange.dydx.trading.feature.portfolio.components.pendingpositions

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxPortfolioPendingPositionsView() {
    DydxThemedPreviewSurface {
        DydxPortfolioPendingPositionsView.Content(
            Modifier,
            DydxPortfolioPendingPositionsView.ViewState.preview,
        )
    }
}

object DydxPortfolioPendingPositionsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val positions: List<DydxPortfolioPendingPositionItemView.ViewState> = listOf(),

    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                positions = listOf(
                    DydxPortfolioPendingPositionItemView.ViewState.preview,
                    DydxPortfolioPendingPositionItemView.ViewState.preview,
                    DydxPortfolioPendingPositionItemView.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioPendingPositionsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        LazyColumn(modifier = modifier) {
            pendingPositionsListContent(state)
        }
    }

    fun LazyListScope.pendingPositionsListContent(state: ViewState?) {
        if (state == null || state.positions.isEmpty()) {
            return
        }

        item {
            Text(
                modifier = Modifier.padding(16.dp),
                text = state.localizer.localize("APP.TRADE.UNOPENED_ISOLATED_POSITIONS"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.large)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )
        }

        items(items = state.positions, key = { it.id }) { position ->
            DydxPortfolioPendingPositionItemView.Content(
                modifier = Modifier
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                state = position,
            )
        }

        item {
            Spacer(modifier = Modifier.padding(ThemeShapes.VerticalPadding))
        }
    }
}
