package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_OrderStatus() {
    DydxThemedPreviewSurface {
        OrderStatusView.Content(Modifier, OrderStatusView.ViewState.preview)
    }
}

object OrderStatusView {
    enum class Status {
        Red, Green, Yellow, Blank;

        val color: ThemeColor.SemanticColor
            get() = when (this) {
                Red -> ThemeColor.SemanticColor.negativeColor
                Green -> ThemeColor.SemanticColor.positiveColor
                Yellow -> ThemeColor.SemanticColor.color_yellow
                Blank -> ThemeColor.SemanticColor.text_primary
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val status: Status? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                status = Status.Red,
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(
                    color = ThemeColor.SemanticColor.layer_0.color,
                    radius = size.minDimension / 2,
                )
            }
            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(
                    color = state.status?.color?.color ?: ThemeColor.SemanticColor.transparent.color,
                    radius = size.minDimension / 2,
                )
            }
        }
    }
}
