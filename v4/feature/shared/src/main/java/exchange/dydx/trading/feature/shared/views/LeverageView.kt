package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.formatter.DydxFormatter

@Preview
@Composable
fun Preview_LeverageView() {
    DydxThemedPreviewSurface {
        LeverageView.Content(Modifier, LeverageView.ViewState.preview)
    }
}

object LeverageView {

    enum class DisplayOption {
        IconOnly, IconAndValue
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val leverage: Double = 1.0,
        val margin: Double? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
            )
        }
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        state: ViewState?,
        textStyle: TextStyle = TextStyle.dydxDefault
            .themeColor(ThemeColor.SemanticColor.text_tertiary),
    ) {
        if (state == null) return

        val leverageText = state.formatter.leverage(state.leverage)
        val leverageIcon = if (state.margin != null) {
            LeverageRiskView.ViewState(
                localizer = state.localizer,
                level = LeverageRiskView.Level.createFromMarginUsage(state.margin),
            )
        } else {
            null
        }

        Row(
            modifier = modifier,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            if (leverageIcon != null) {
                LeverageRiskView.Content(
                    modifier = Modifier,
                    state = leverageIcon.copy(
                        displayOption = LeverageRiskView.DisplayOption.IconOnly,
                        viewSize = 14.dp,
                    ),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            CreateValueText(Modifier, leverageText, textStyle)
        }
    }

    @Composable
    private fun CreateValueText(
        modifier: Modifier,
        value: String?,
        textStyle: TextStyle,
    ) {
        Text(
            modifier = modifier,
            text = value ?: "-",
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
