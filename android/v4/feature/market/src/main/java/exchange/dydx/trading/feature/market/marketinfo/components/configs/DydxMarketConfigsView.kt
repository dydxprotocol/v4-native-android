package exchange.dydx.trading.feature.market.marketinfo.components.configs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.TokenTextView

@Preview
@Composable
fun Preview_DydxMarketConfigsView() {
    DydxThemedPreviewSurface {
        DydxMarketConfigsView.Content(Modifier, DydxMarketConfigsView.ViewState.preview)
    }
}

object DydxMarketConfigsView : DydxComponent {

    data class Item(
        val title: String,
        val value: String,
        val tokenText: TokenTextView.ViewState? = null,
    )

    data class ViewState(
        val localizer: LocalizerProtocol,
        val items: List<Item>?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                items = listOf(
                    Item(
                        title = "title",
                        value = "value",
                    ),
                    Item(
                        title = "title",
                        value = "value",
                    ),
                    Item(
                        title = "title",
                        value = "value",
                        tokenText = TokenTextView.ViewState.preview,
                    ),
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketConfigsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding),
        ) {
            state?.items?.forEach { item ->
                Row(
                    modifier = Modifier
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.title,
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = item.value,
                        style = TextStyle.dydxDefault
                            .themeFont(
                                fontSize = ThemeFont.FontSize.base,
                                fontType = ThemeFont.FontType.plus,
                            ),
                    )
                    if (item.tokenText != null) {
                        TokenTextView.Content(
                            modifier = Modifier.padding(start = 8.dp),
                            state = item.tokenText,
                            textStyle = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.mini),
                        )
                    }
                }

                if (item !== state.items.last()) {
                    PlatformDivider()
                }
            }
        }
    }
}
