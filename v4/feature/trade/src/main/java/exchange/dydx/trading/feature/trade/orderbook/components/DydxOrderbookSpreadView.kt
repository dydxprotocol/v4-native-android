package exchange.dydx.trading.feature.trade.orderbook.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
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
fun Preview_DydxOrderbookSpreadView() {
    DydxThemedPreviewSurface {
        DydxOrderbookSpreadView.Content(Modifier, DydxOrderbookSpreadView.ViewState.preview)
    }
}

object DydxOrderbookSpreadView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val percent: String?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                percent = "0.1%",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxOrderbookSpreadViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Row(
            modifier = modifier.padding(horizontal = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.localizer.localize("APP.TRADE.ORDERBOOK_SPREAD"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.mini),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = state.percent ?: "",
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_primary)
                    .themeFont(fontSize = ThemeFont.FontSize.mini, fontType = ThemeFont.FontType.number),
            )
        }
    }
}
