package exchange.dydx.trading.feature.trade.trigger.components.inputfields.limitprice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.inputs.PlatformSwitchInput
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.navigation.DydxAnimation
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.DydxTriggerOrderPriceInputType
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.price.DydxTriggerOrderPriceView

@Preview
@Composable
fun Preview_DydxTriggerOrderLimitPriceSectionView() {
    DydxThemedPreviewSurface {
        DydxTriggerOrderLimitPriceSectionView.Content(
            Modifier,
            DydxTriggerOrderLimitPriceSectionView.ViewState.preview,
        )
    }
}

object DydxTriggerOrderLimitPriceSectionView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val enabled: Boolean = true,
        val onEnabledChanged: (Boolean) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTriggerOrderLimitPriceSectionViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlatformSwitchInput(
                modifier = Modifier.fillMaxWidth(),
                label = state.localizer.localize("APP.TRADE.LIMIT_PRICE"),
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.base)
                    .themeColor(ThemeColor.SemanticColor.text_secondary),
                value = state.enabled,
                onValueChange = state.onEnabledChanged,
            )

            DydxAnimation.AnimateExpandInOut(
                visible = state.enabled,
            ) {
                LimitPricesContent(Modifier)
            }
        }
    }

    @Composable
    private fun LimitPricesContent(modifier: Modifier) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DydxTriggerOrderPriceView.Content(
                modifier = Modifier.fillMaxWidth().weight(1f),
                inputType = DydxTriggerOrderPriceInputType.TakeProfitLimit,
            )

            DydxTriggerOrderPriceView.Content(
                modifier = Modifier.fillMaxWidth().weight(1f),
                inputType = DydxTriggerOrderPriceInputType.StopLossLimit,
            )
        }
    }
}
