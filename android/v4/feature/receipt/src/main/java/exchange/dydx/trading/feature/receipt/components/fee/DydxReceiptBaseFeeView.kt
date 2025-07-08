package exchange.dydx.trading.feature.receipt.components.fee

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.receipt.components.DydxReceiptEmptyView

@Preview
@Composable
fun Preview_DydxReceiptBaseFeeView() {
    DydxThemedPreviewSurface {
        DydxReceiptBaseFeeView.Content(Modifier, DydxReceiptBaseFeeView.ViewState.preview)
    }
}

open class DydxReceiptBaseFeeView {
    sealed class FeeFont {
        data class Number(val value: kotlin.String) : FeeFont()
        data class String(val value: kotlin.String) : FeeFont()
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val feeType: String? = null,
        val feeFont: FeeFont? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                feeType = "Taker",
                feeFont = FeeFont.Number("111"),
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
                    text = state.localizer.localize("APP.TRADE.FEE"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = state.feeType ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                Spacer(modifier = Modifier.weight(0.1f))

                when (state.feeFont) {
                    is FeeFont.Number -> {
                        Text(
                            text = state.feeFont.value,
                            style = TextStyle.dydxDefault
                                .themeFont(
                                    fontSize = ThemeFont.FontSize.small,
                                    fontType = ThemeFont.FontType.number,
                                )
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    }

                    is FeeFont.String -> {
                        Text(
                            text = state.feeFont.value,
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.small)
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    }

                    else -> {
                        DydxReceiptEmptyView()
                    }
                }
            }
        }
    }
}
