package exchange.dydx.trading.feature.portfolio.components.fundings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle

@Preview
@Composable
fun Preview_DydxPortfolioFundingsView() {
    DydxThemedPreviewSurface {
        LazyColumn {
            DydxPortfolioFundingsView.ListContent(
                this,
                Modifier,
                DydxPortfolioFundingsView.ViewState.preview,
            )
        }
    }
}

object DydxPortfolioFundingsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val text: String?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                text = "1.0M",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioFundingsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        LazyColumn {
            ListContent(this, modifier, state)
        }
    }

    fun ListContent(scope: LazyListScope, modifier: Modifier, state: ViewState?) {
        if (state == null) return

        scope.item(key = "header") {
            CreateHeader(modifier, state)
        }

        // TODO: Implement

//        if (state.fills.isEmpty()) {
//            scope.item(key = "placeholder") {
//                DydxPortfolioPlaceholderView.Content(modifier.padding(vertical = 32.dp))
//            }
//        } else {
//            scope.items(items = state.fills, key = { it.id }) { fill ->
//                if (fill === state.fills.first()) {
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//                DydxPortfolioFillItemView.Content(
//                    modifier = modifier
//                        .clickable { state.onFillTappedAction(fill.id) },
//                    state = fill,
//                )
//
//                if (fill !== state.fills.last()) {
//                    PlatformDivider()
//                }
//            }
//        }
    }

    @Composable
    private fun CreateHeader(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding * 2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.width(80.dp),
                text = state.localizer.localize("APP.GENERAL.TIME"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Text(
                text = state.localizer.localize("APP.GENERAL.TYPE_AMOUNT"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = state.localizer.localize("APP.GENERAL.PRICE_FEE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }
}
