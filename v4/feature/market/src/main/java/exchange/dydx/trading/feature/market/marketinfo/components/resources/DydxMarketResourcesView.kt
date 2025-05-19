package exchange.dydx.trading.feature.market.marketinfo.components.resources

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState

@Preview
@Composable
fun Preview_DydxMarketResourcesView() {
    DydxThemedPreviewSurface {
        DydxMarketResourcesView.Content(Modifier, DydxMarketResourcesView.ViewState.preview)
    }
}

object DydxMarketResourcesView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val sharedMarketViewState: SharedMarketViewState?,
        val urlHandler: (String) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                sharedMarketViewState = SharedMarketViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketResourcesViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier
                .padding(horizontal = ThemeShapes.HorizontalPadding),
        ) {
            HeaderView(state = state)

            state.sharedMarketViewState?.primaryDescription?.let {
                Spacer(modifier = Modifier.padding(ThemeShapes.VerticalPadding))
                Text(
                    text = it,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base),
                )
            }

            state.sharedMarketViewState?.secondaryDescription?.let {
                Spacer(modifier = Modifier.padding(ThemeShapes.VerticalPadding))
                Text(
                    text = it,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }
        }
    }

    @Composable
    private fun HeaderView(
        state: ViewState,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding)
                .padding(top = ThemeShapes.VerticalPadding)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            PlatformRoundImage(
                icon = state.sharedMarketViewState?.logoUrl,
                size = 40.dp,
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .align(Alignment.CenterVertically)
                    .weight(1f),
            ) {
                Text(
                    text = state.sharedMarketViewState?.tokenFullName ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontType = ThemeFont.FontType.plus,
                            fontSize = ThemeFont.FontSize.base,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                Text(
                    text = state.sharedMarketViewState?.id?.uppercase() ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(30.dp),
            ) {
                CreateButton(state.sharedMarketViewState?.coinMarketPlaceUrl, R.drawable.icon_coinmarketcap, state.urlHandler)
                CreateButton(state.sharedMarketViewState?.whitepaperUrl, R.drawable.icon_whitepaper, state.urlHandler)
                CreateButton(state.sharedMarketViewState?.websiteUrl, R.drawable.icon_web, state.urlHandler)
            }
        }
    }

    @Composable
    private fun CreateButton(
        url: String?,
        icon: Any?,
        urlHandler: (String) -> Unit = {},
    ) {
        if (url != null) {
            PlatformImage(
                icon = icon,
                modifier = Modifier
                    .clickable { urlHandler(url) }
                    .size(30.dp),
            )
        }
    }
}
