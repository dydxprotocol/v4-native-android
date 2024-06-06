package exchange.dydx.trading.feature.trade.margin.components.liquidationprice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.changes.PlatformDirection
import exchange.dydx.platformui.components.changes.PlatformDirectionArrow
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.feature.shared.views.AmountText

@Preview
@Composable
fun Preview_DydxAdjustMarginInputLiquidationPriceView() {
    DydxThemedPreviewSurface {
        DydxAdjustMarginInputLiquidationPriceView.Content(
            Modifier,
            DydxAdjustMarginInputLiquidationPriceView.ViewState.preview,
        )
    }
}

object DydxAdjustMarginInputLiquidationPriceView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val before: AmountText.ViewState? = null,
        val after: AmountText.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                before = AmountText.ViewState.preview,
                after = AmountText.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAdjustMarginInputLiquidationPriceViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val shape = RoundedCornerShape(8.dp)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(color = exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_5.color, shape = shape)
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = ThemeShapes.VerticalPadding),
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = state.localizer.localize("APP.GENERAL.ESTIMATED"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
                Text(
                    text = state.localizer.localize("APP.TRADE.LIQUIDATION_PRICE"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_secondary)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                if (state.before != null) {
                    AmountText.Content(
                        state = state.before,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                }
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    PlatformDirectionArrow(
                        direction = PlatformDirection.None,
                        modifier = Modifier.size(12.dp),
                    )
                    AmountText.Content(
                        state = state.after,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.medium, fontType = ThemeFont.FontType.number)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
