package exchange.dydx.trading.feature.receipt.components.ordercount

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.changes.PlatformAmountChange
import exchange.dydx.platformui.components.changes.PlatformDirection
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.formatter.DydxFormatter

@Preview
@Composable
fun Preview_DydxReceiptOrderCountView() {
    DydxThemedPreviewSurface {
        DydxReceiptOrderCountView.Content(Modifier, DydxReceiptOrderCountView.ViewState.preview)
    }
}

object DydxReceiptOrderCountView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val before: Int? = null,
        val after: Int? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxReceiptOrderCountViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = state.localizer.localize("APP.CANCEL_ORDERS_MODAL.OPEN_ORDERS"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Spacer(modifier = Modifier.weight(0.1f))

            PlatformAmountChange(
                before = state.before?.let {
                    {
                        Text(
                            text = state.formatter.localFormatted(it.toDouble(), 0) ?: "",
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                                .themeColor(ThemeColor.SemanticColor.text_tertiary),
                        )
                    }
                },
                after = state.after?.let {
                    {
                        Text(
                            text = state.formatter.localFormatted(it.toDouble(), 0) ?: "",
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    }
                },
                direction = PlatformDirection.from(state.after?.toDouble(), state.before?.toDouble()),
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )
        }
    }
}
