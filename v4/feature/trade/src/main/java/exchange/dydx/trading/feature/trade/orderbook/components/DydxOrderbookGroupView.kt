package exchange.dydx.trading.feature.trade.orderbook.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformIconButton
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.textgroups.PlatformAutoSizingText
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxOrderbookGroupView() {
    DydxThemedPreviewSurface {
        DydxOrderbookGroupView.Content(Modifier, DydxOrderbookGroupView.ViewState.preview)
    }
}

object DydxOrderbookGroupView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val price: String? = null,
        val zoomLevel: Int = 0,
        val onZoomed: (Int) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                price = "$1.0",
            )
        }
    }

    private const val MaxZoom = 3

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxOrderbookGroupViewModel = hiltViewModel()

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
            PlatformAutoSizingText(
                modifier = Modifier.weight(1f),
                text = state.price ?: "",
                textStyle = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number),
            )
            ZoomButton(
                icon = R.drawable.icon_minus,
                enabled = state.zoomLevel > 0,
            ) {
                if (state.zoomLevel <= 0) return@ZoomButton
                state.onZoomed(state.zoomLevel - 1)
            }
            ZoomButton(
                icon = R.drawable.icon_plus,
                enabled = state.zoomLevel < MaxZoom,
            ) {
                if (state.zoomLevel >= MaxZoom) return@ZoomButton
                state.onZoomed(state.zoomLevel + 1)
            }
        }
    }

    @Composable
    private fun ZoomButton(
        icon: Any,
        enabled: Boolean,
        action: (() -> Unit),
    ) {
        PlatformIconButton(
            size = 24.dp,
            action = action,
            enabled = enabled,
            backgroundColor = if (enabled) ThemeColor.SemanticColor.layer_5 else ThemeColor.SemanticColor.layer_0,
        ) {
            PlatformImage(
                modifier = Modifier.size(14.dp),
                icon = icon,
            )
        }
    }
}
