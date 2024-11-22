package exchange.dydx.trading.feature.receipt.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_DydxReceiptItemView() {
    DydxThemedPreviewSurface {
        DydxReceiptItemView.Content(Modifier, DydxReceiptItemView.ViewState.preview)
    }
}

open class DydxReceiptItemView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val title: String? = null,
        val value: String? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                title = "Title",
                value = "$30.00",
            )
        }
    }

    companion object {
        @Composable
        fun Content(modifier: Modifier, state: ViewState?) {
            if (state == null) return

            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = state.title ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )

                Spacer(modifier = Modifier.weight(0.1f))

                if (state.value != null) {
                    Text(
                        text = state.value,
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                } else {
                    DydxReceiptEmptyView()
                }
            }
        }
    }
}
