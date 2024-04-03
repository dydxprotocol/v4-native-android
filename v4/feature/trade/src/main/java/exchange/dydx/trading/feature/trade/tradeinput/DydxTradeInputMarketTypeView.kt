package exchange.dydx.trading.feature.trade.tradeinput

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.platformui.components.PlatformInfoScaffold
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.navigation.DydxAnimation
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton

@Preview
@Composable
fun Preview_DydxTradeInputMarketTypeView() {
    DydxThemedPreviewSurface {
        DydxTradeInputMarketTypeView.Content(
            Modifier,
            DydxTradeInputMarketTypeView.ViewState.preview
        )
    }
}

object DydxTradeInputMarketTypeView : DydxComponent {
    data class MarginTypeSelection(
        val title: String,
        val text: String,
        val action: () -> Unit,
    )

    data class ViewState(
        val title: String,
        val asset: String,
        val crossMargin: MarginTypeSelection,
        val isolatedMargin: MarginTypeSelection,
        val errorText: String?,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                title = "Margin Mode",
                asset = "for ETH-USD",
                crossMargin = MarginTypeSelection(
                    title = "Cross Margin",
                    text = "This is the description text for cross margin",
                    action = {},
                ),
                isolatedMargin = MarginTypeSelection(
                    title = "Isolated Margin",
                    text = "This is the description text for isolated margin",
                    action = {},
                ),
                errorText = "Error",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputMarketTypeViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        PlatformInfoScaffold(modifier = modifier, platformInfo = viewModel.platformInfo) {
            Content(modifier, state)
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .animateContentSize()
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_3),
        ) {
            Row(
                modifier
                    .fillMaxWidth()
                    .padding(vertical = ThemeShapes.VerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 0.dp),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.large,
                            fontType = ThemeFont.FontType.plus,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                    text = state.title,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.large,
                            fontType = ThemeFont.FontType.plus,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                    text = state.asset,
                )
                Spacer(modifier = Modifier.weight(1f))
                HeaderViewCloseBotton(
                    closeAction = state.closeAction,
                )
            }

            PlatformDivider()



        }
    }
}

