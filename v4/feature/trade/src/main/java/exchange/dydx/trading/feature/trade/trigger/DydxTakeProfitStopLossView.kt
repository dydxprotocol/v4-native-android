package exchange.dydx.trading.feature.trade.trigger

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import exchange.dydx.abacus.protocols.LocalizerProtocol
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
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton
import exchange.dydx.trading.feature.trade.trigger.components.DydxTakeProfitStopLossCtaButtonView
import exchange.dydx.trading.feature.trade.trigger.components.DydxTakeProfitStopLossReceiptView

@Preview
@Composable
fun Preview_DydxTakeProfitStopLossView() {
    DydxThemedPreviewSurface {
        DydxTakeProfitStopLossView.Content(Modifier, DydxTakeProfitStopLossView.ViewState.preview)
    }
}

object DydxTakeProfitStopLossView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTakeProfitStopLossViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val focusManager = LocalFocusManager.current

        Column(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
        ) {
            HeaderView(modifier = Modifier, state = state)

            DydxTakeProfitStopLossReceiptView.Content(
                modifier = Modifier.padding(top = 16.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            DydxTakeProfitStopLossCtaButtonView.Content(
                Modifier
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
}
