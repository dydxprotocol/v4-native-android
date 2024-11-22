package exchange.dydx.trading.feature.receipt.components.marginusage

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
import exchange.dydx.trading.feature.shared.views.MarginUsageView

@Preview
@Composable
fun Preview_DydxReceiptMarginUsageView() {
    DydxThemedPreviewSurface {
        DydxReceiptMarginUsageView.Content(Modifier, DydxReceiptMarginUsageView.ViewState.preview)
    }
}

object DydxReceiptMarginUsageView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val label: String? = null,
        val before: MarginUsageView.ViewState? = null,
        val after: MarginUsageView.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                before = MarginUsageView.ViewState.preview,
                after = MarginUsageView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxReceiptMarginUsageViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = state.label ?: state.localizer.localize("APP.GENERAL.MARGIN_USAGE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Spacer(modifier = Modifier.weight(0.1f))

            PlatformAmountChange(
                modifier = Modifier,
                before = if (state.before != null) { {
                    MarginUsageView.Content(
                        state = state.before,
                        formatter = state.formatter,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                } } else {
                    null
                },
                after =
                if (state.after != null) { {
                    MarginUsageView.Content(
                        state = state.after,
                        formatter = state.formatter,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                } } else {
                    null
                },
                direction = PlatformDirection.from(state.after?.percent, state.before?.percent),
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }
}
