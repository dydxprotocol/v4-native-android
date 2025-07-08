package exchange.dydx.trading.feature.trade.marginmode

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton

@Preview
@Composable
fun Preview_DydxTradeInputMarginModeView() {
    DydxThemedPreviewSurface {
        DydxTradeInputMarginModeView.Content(
            Modifier,
            DydxTradeInputMarginModeView.ViewState.preview,
        )
    }
}

object DydxTradeInputMarginModeView : DydxComponent {
    data class MarginTypeSelection(
        val title: String,
        val text: String,
        val selected: Boolean,
        val action: () -> Unit,
    )

    data class ViewState(
        val title: String,
        val asset: String,
        val crossMargin: MarginTypeSelection,
        val isolatedMargin: MarginTypeSelection,
        val errorText: String?,
        val logoUrl: String? = null,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                title = "Margin Mode",
                asset = "for ETH-USD",
                crossMargin = MarginTypeSelection(
                    title = "Cross Margin",
                    text = "This is the description text for cross margin",
                    selected = true,
                    action = {},
                ),
                isolatedMargin = MarginTypeSelection(
                    title = "Isolated Margin",
                    text = "This is the description text for isolated margin",
                    selected = false,
                    action = {},
                ),
                errorText = "Error",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputMarginModeViewModel = hiltViewModel()

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
                .animateContentSize()
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_3),
        ) {
            NavigationHeader(
                modifier = Modifier,
                state = state,
            )
            PlatformDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Selection(
                modifier = Modifier,
                marginModeState = state.crossMargin,
            )
            Selection(
                modifier = Modifier,
                marginModeState = state.isolatedMargin,
            )
        }
    }

    @Composable
    private fun NavigationHeader(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlatformRoundImage(
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                icon = state.logoUrl,
                size = 40.dp,
            )

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
    }

    @Composable
    private fun Selection(
        modifier: Modifier,
        marginModeState: MarginTypeSelection
    ) {
        val shape = RoundedCornerShape(10.dp)
        Row(
            modifier = modifier
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                )
                .fillMaxWidth()
                .background(
                    color = if (marginModeState.selected) {
                        ThemeColor.SemanticColor.layer_1.color
                    } else {
                        ThemeColor.SemanticColor.layer_4.color
                    },
                    shape = shape,
                )
                .border(
                    width = 1.dp,
                    color = if (marginModeState.selected) {
                        ThemeColor.SemanticColor.color_purple.color
                    } else {
                        ThemeColor.SemanticColor.layer_7.color
                    },
                    shape = shape,
                )
                .clip(shape)
                .clickable { marginModeState.action() }
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = 16.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 0.dp),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.medium,
                            fontType = ThemeFont.FontType.book,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                    text = marginModeState.title,
                )

                Text(
                    modifier = Modifier
                        .padding(horizontal = 0.dp),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.small,
                            fontType = ThemeFont.FontType.book,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    text = marginModeState.text,
                )
            }
        }
    }
}
