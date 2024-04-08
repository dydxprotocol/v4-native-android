package exchange.dydx.trading.feature.trade.trigger

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.OnLifecycleEvent
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
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.views.HeaderView
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton
import exchange.dydx.trading.feature.trade.trigger.components.DydxTriggerOrderCtaButtonView
import exchange.dydx.trading.feature.trade.trigger.components.DydxTriggerOrderReceiptView
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.DydxTriggerOrderInputType
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.DydxTriggerOrderPriceInputType
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.gainloss.DydxTriggerOrderGainLossView
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.headersection.DydxTriggerSectionHeaderView
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.limitprice.DydxTriggerOrderLimitPriceSectionView
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.price.DydxTriggerOrderPriceView
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.size.DydxTriggerOrderSizeView

@Preview
@Composable
fun Preview_DydxTriggerOrderInputView() {
    DydxThemedPreviewSurface {
        DydxTriggerOrderInputView.Content(Modifier, DydxTriggerOrderInputView.ViewState.preview)
    }
}

object DydxTriggerOrderInputView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val closeAction: (() -> Unit)? = null,
        val backHandler: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTriggerOrderInputViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        OnLifecycleEvent { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                state?.backHandler?.invoke()
            }
        }

        PlatformInfoScaffold(
            platformInfo = viewModel.platformInfo,
        ) {
            Content(modifier, state)
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val focusManager = LocalFocusManager.current
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HeaderView(modifier = Modifier, state = state)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DydxTriggerOrderReceiptView.Content(
                    modifier = Modifier,
                )

                TakeProfitSectionView(
                    modifier = Modifier,
                    state = state,
                )

                StopLossSectionView(
                    modifier = Modifier,
                    state = state,
                )

                AdvancedDividerView(
                    modifier = Modifier,
                    state = state,
                )

                DydxTriggerOrderSizeView.Content(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ThemeShapes.HorizontalPadding),
                )

                DydxTriggerOrderLimitPriceSectionView.Content(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ThemeShapes.HorizontalPadding),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            DydxTriggerOrderCtaButtonView.Content(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(bottom = ThemeShapes.VerticalPadding * 2),
            )
        }
    }

    @Composable
    private fun HeaderView(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.TRIGGERS_MODAL.PRICE_TRIGGERS"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.extra)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                Text(
                    text = state.localizer.localize("APP.TRIGGERS_MODAL.PRICE_TRIGGERS_DESCRIPTION"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }
            HeaderViewCloseBotton(
                modifier = Modifier,
                closeAction = state.closeAction,
            )
        }
    }

    @Composable
    private fun TakeProfitSectionView(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding),
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.localizer.localize("TRADE.BRACKET_ORDER_TP.TITLE"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )

                DydxTriggerSectionHeaderView.Content(
                    modifier = Modifier.weight(1f),
                    inputType = DydxTriggerOrderInputType.TakeProfit,
                )
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DydxTriggerOrderPriceView.Content(
                    modifier = Modifier.weight(1f),
                    inputType = DydxTriggerOrderPriceInputType.TakeProfit,
                )

                DydxTriggerOrderGainLossView.Content(
                    modifier = Modifier.weight(1f),
                    inputType = DydxTriggerOrderInputType.TakeProfit,
                )
            }
        }
    }

    @Composable
    private fun StopLossSectionView(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding),
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.localizer.localize("TRADE.BRACKET_ORDER_SL.TITLE"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )

                DydxTriggerSectionHeaderView.Content(
                    modifier = Modifier.weight(1f),
                    inputType = DydxTriggerOrderInputType.StopLoss,
                )
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DydxTriggerOrderPriceView.Content(
                    modifier = Modifier.weight(1f),
                    inputType = DydxTriggerOrderPriceInputType.StopLoss,
                )

                DydxTriggerOrderGainLossView.Content(
                    modifier = Modifier.weight(1f),
                    inputType = DydxTriggerOrderInputType.StopLoss,
                )
            }
        }
    }

    @Composable
    private fun AdvancedDividerView(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.ADVANCED"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
            PlatformDivider(
                modifier = Modifier.weight(1f),
            )
        }
    }
}
