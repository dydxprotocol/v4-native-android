package exchange.dydx.trading.feature.receipt.components.leverage

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
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.feature.shared.views.LeverageView

@Preview
@Composable
fun Preview_DydxReceiptLeverageView() {
    DydxThemedPreviewSurface {
        DydxReceiptPositionLeverageView.Content(Modifier, DydxReceiptPositionLeverageView.ViewState.preview)
    }
}

object DydxReceiptPositionLeverageView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val before: LeverageView.ViewState? = null,
        val after: LeverageView.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                before = LeverageView.ViewState.preview,
                after = LeverageView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxReceiptPositionLeverageViewModel = hiltViewModel()

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
                text = state.localizer.localize("APP.TRADE.POSITION_LEVERAGE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Spacer(modifier = Modifier.weight(0.1f))

            PlatformAmountChange(
                modifier = Modifier.weight(1f),
                before = if (state.before != null) { {
                    LeverageView.Content(
                        state = state.before,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                } } else {
                    null
                },
                after =
                if (state.after != null) { {
                    LeverageView.Content(
                        state = state.after,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                } } else {
                    null
                },
                direction = PlatformDirection.from(state.after?.leverage, state.before?.leverage),
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }
}