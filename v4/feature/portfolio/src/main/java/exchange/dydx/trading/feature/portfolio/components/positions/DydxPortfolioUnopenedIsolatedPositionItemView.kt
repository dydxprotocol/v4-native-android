package exchange.dydx.trading.feature.portfolio.components.positions

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketPositionViewState

@Preview
@Composable
fun Preview_DydxPortfolioUnopenedIsolatedPositionItemView() {
    DydxThemedPreviewSurface {
        DydxPortfolioUnopenedIsolatedPositionItemView.Content(
            Modifier,
            DydxPortfolioUnopenedIsolatedPositionItemView.ViewState.preview
        )
    }
}

object DydxPortfolioUnopenedIsolatedPositionItemView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val id: String,
        val logoUrl: String? = null,
        val equity: Double,
        val onViewMarket: ((id: String) -> Unit)? = null,
        val onCancel: ((id: String) -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                id = "ETH-USD",
                equity = 1.0,
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        val state = state ?: return
        val shape = RoundedCornerShape(10.dp)
        Row(
            modifier = modifier
                .padding(
                    // outer padding first, before width and height
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                )
                .fillMaxWidth()
                .height(102.dp + ThemeShapes.VerticalPadding * 2)
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = shape,
                )
                .clip(shape)
                .clickable { state.onViewMarket?.invoke(state.id) }
                .padding(
                    // inner paddings after clipping
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ComposeAssetPosition(
                        state,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = state.localizer.localize("APP.GENERAL.MARGIN"),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                    Spacer(modifier = Modifier.weight(1.0f))

                    Text(
                        text = state.formatter.dollar(state.equity, 2) ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.text_secondary),
                    )
                }

                PlatformDivider(
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = state.localizer.localize("APP.CLOSE_POSITIONS_CONFIRMATION_TOAST.VIEW_ORDERS"),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.color_purple),
                    )
                    Spacer(modifier = Modifier.weight(1.0f))

                    Text(
                        text = state.localizer.localize("APP.GENERAL.CANCEL"),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.color_red),
                    )
                }
            }
        }
    }

    @Composable
    fun ComposeAssetPosition(
        state: ViewState,
    ) {
        PlatformRoundImage(
            icon = state.logoUrl,
            size = 36.dp,
        )
        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = state.id,
            style = TextStyle.dydxDefault
                .themeFont(fontSize = ThemeFont.FontSize.small)
                .themeColor(ThemeColor.SemanticColor.text_secondary),
        )
    }
}

