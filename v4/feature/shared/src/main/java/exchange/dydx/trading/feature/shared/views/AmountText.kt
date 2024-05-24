package exchange.dydx.trading.feature.shared.views

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.utilities.utils.NumericFilter
import exchange.dydx.utilities.utils.filter

@Preview
@Composable
fun Preview_AmountText() {
    DydxThemedPreviewSurface {
        AmountText.Content(Modifier, AmountText.ViewState.preview)
    }
}

object AmountText {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val amount: Double?,
        val tickSize: Int?,
        val requiresPositive: Boolean = false,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                amount = 1.0,
                tickSize = 2,
            )
        }
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        state: ViewState?,
        textStyle: TextStyle = TextStyle.dydxDefault
            .themeFont(fontType = ThemeFont.FontType.number, fontSize = ThemeFont.FontSize.small),
    ) {
        if (state == null) return

        val amount = if (state.requiresPositive) {
            state.amount?.filter(NumericFilter.NotNegative)
        } else {
            state.amount
        }

        Text(
            modifier = modifier,
            text = state.formatter.dollar(amount, state.tickSize ?: 2) ?: "",
            style = textStyle,
        )
    }
}
