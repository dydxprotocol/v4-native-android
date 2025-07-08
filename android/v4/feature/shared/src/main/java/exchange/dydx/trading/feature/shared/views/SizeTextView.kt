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

@Preview
@Composable
fun Preview_SizeTextView() {
    DydxThemedPreviewSurface {
        SizeTextView.Content(Modifier, SizeTextView.ViewState.preview)
    }
}

object SizeTextView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val size: Double?,
        val stepSize: Int? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                size = 123.4,
                stepSize = 1,
            )
        }
    }

    @Composable
    fun Content(
        modifier: Modifier,
        state: ViewState?,
        textStyle: TextStyle = TextStyle.dydxDefault
            .themeFont(fontSize = ThemeFont.FontSize.small),
    ) {
        if (state == null) return

        val sizeText = state.formatter.localFormatted(state.size ?: 0.0, state.stepSize ?: 0)
        if (sizeText.isNullOrEmpty()) return

        Text(
            modifier = modifier,
            text = sizeText,
            style = textStyle,
        )
    }
}
