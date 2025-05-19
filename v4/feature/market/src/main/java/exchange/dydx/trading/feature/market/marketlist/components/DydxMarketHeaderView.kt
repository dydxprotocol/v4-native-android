package exchange.dydx.trading.feature.market.marketlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformIconButton
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxMarketHeaderView() {
    DydxThemedPreviewSurface {
        DydxMarketHeaderView.Content(Modifier, DydxMarketHeaderView.ViewState.preview)
    }
}

object DydxMarketHeaderView : DydxComponent {

    data class ViewState(
        val localizer: LocalizerProtocol,
        val searchAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketHeaderViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Row(modifier = modifier) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.MARKETS"),
                    modifier = Modifier,
                    style = TextStyle.dydxDefault
                        .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.extra)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
            }

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.SEARCH"),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.small,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                PlatformIconButton(
                    action = state.searchAction ?: {},
                    size = 42.dp,
                ) {
                    Icon(
                        painter = painterResource(id = exchange.dydx.trading.feature.shared.R.drawable.icon_search),
                        contentDescription = state.localizer.localize("APP.GENERAL.SEARCH"),
                        modifier = Modifier,
                        tint = ThemeColor.SemanticColor.text_primary.color,
                    )
                }
            }
        }
    }
}
