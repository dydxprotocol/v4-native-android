package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.progress.PlatformCircularProgress
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.formatter.DydxFormatter

@Preview
@Composable
fun Preview_MarginUsageView() {
    DydxThemedPreviewSurface {
        MarginUsageView.Content(Modifier, MarginUsageView.ViewState.preview, DydxFormatter())
    }
}

object MarginUsageView {

    enum class DisplayOption {
        IconOnly, IconAndValue
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val displayOption: DisplayOption = DisplayOption.IconOnly,
        val lineWidth: Double = 3.0,
        val size: Dp = 16.0.dp,
        val percent: Double = 0.5,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        state: ViewState?,
        formatter: DydxFormatter,
        textStyle: TextStyle = TextStyle.dydxDefault,
    ) {
        if (state == null) return

        val color: ThemeColor.SemanticColor = when {
            state.percent <= 0.2 -> ThemeColor.SemanticColor.color_green
            state.percent <= 0.4 -> ThemeColor.SemanticColor.color_yellow
            else -> ThemeColor.SemanticColor.color_red
        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlatformCircularProgress(
                modifier = Modifier.size(state.size),
                progress = state.percent,
                lineWidth = state.lineWidth,
                innerTrackColor = color,
                outerTrackColor = color,
            )

            Spacer(modifier = Modifier.size(6.dp))

            if (state.displayOption == DisplayOption.IconAndValue) {
                Text(
                    text = formatter.percent(state.percent, digits = 2) ?: "-",
                    style = textStyle,
                    maxLines = 1,
                )
            }
        }
    }
}
