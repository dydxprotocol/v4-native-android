package exchange.dydx.trading.feature.trade.trigger.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Preview
@Composable
fun Preview_DydxTriggerOrderReceiptView() {
    DydxThemedPreviewSurface {
        DydxTriggerOrderReceiptView.Content(
            Modifier,
            DydxTriggerOrderReceiptView.ViewState.preview,
        )
    }
}

object DydxTriggerOrderReceiptView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val entryPrice: String? = null,
        val oraclePrice: String? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                entryPrice = "$1.00",
                oraclePrice = "$2.00",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTriggerOrderReceiptViewModel = hiltViewModel()

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
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .background(
                    color = ThemeColor.SemanticColor.layer_0.color,
                    shape = RoundedCornerShape(8.dp),
                ),
        ) {
            Column(
                modifier = Modifier
                    .padding(ThemeShapes.HorizontalPadding),
            ) {
                ReceiptLine(
                    modifier = Modifier,
                    label = state.localizer.localize("APP.TRIGGERS_MODAL.AVG_ENTRY_PRICE"),
                    value = state.entryPrice ?: "-",
                )

                ReceiptLine(
                    modifier = Modifier,
                    label = state.localizer.localize("APP.TRADE.ORACLE_PRICE"),
                    value = state.oraclePrice ?: "-",
                )
            }
        }
    }

    @Composable
    private fun ReceiptLine(modifier: Modifier, label: String, value: String) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = label,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Text(
                text = value,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.base, fontType = ThemeFont.FontType.number)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )
        }
    }
}
