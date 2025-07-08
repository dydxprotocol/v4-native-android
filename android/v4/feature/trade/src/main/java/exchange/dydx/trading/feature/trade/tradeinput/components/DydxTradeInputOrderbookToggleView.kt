package exchange.dydx.trading.feature.trade.tradeinput.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputView

@Preview
@Composable
fun Preview_DydxTradeInputOrderbookToggleView() {
    DydxThemedPreviewSurface {
        DydxTradeInputOrderbookToggleView.Content(
            Modifier,
            DydxTradeInputOrderbookToggleView.ViewState.preview,
        )
    }
}

object DydxTradeInputOrderbookToggleView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val toggleState: DydxTradeInputView.OrderbookToggleState = DydxTradeInputView.OrderbookToggleState.Open,
        val onToggleAction: (DydxTradeInputView.OrderbookToggleState) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputOrderbookToggleViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val toggleState: MutableState<DydxTradeInputView.OrderbookToggleState> = remember {
            mutableStateOf(state.toggleState)
        }

        val shape = RoundedCornerShape(size = 10.dp)

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .height(44.dp)
                .background(ThemeColor.SemanticColor.layer_4.color, shape)
                .border(
                    width = 1.dp,
                    color = ThemeColor.SemanticColor.layer_6.color,
                    shape = shape,
                )
                .clip(shape)
                .clickable {
                    toggleState.value = when (toggleState.value) {
                        DydxTradeInputView.OrderbookToggleState.Open -> {
                            DydxTradeInputView.OrderbookToggleState.Closed
                        }

                        DydxTradeInputView.OrderbookToggleState.Closed -> {
                            DydxTradeInputView.OrderbookToggleState.Open
                        }
                    }
                    state.onToggleAction(toggleState.value)
                },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .animateContentSize()
                    .padding(horizontal = 16.dp),
            ) {
                AnimatedVisibility(
                    visible = toggleState.value == DydxTradeInputView.OrderbookToggleState.Closed,
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = TextStyle.dydxDefault
                            .themeFont(
                                fontSize = ThemeFont.FontSize.base,
                                fontType = ThemeFont.FontType.plus,
                            ),
                        text = state.localizer.localize("APP.TRADE.ORDERBOOK"),
                    )
                }

                PlatformImage(
                    icon = if (toggleState.value == DydxTradeInputView.OrderbookToggleState.Closed) {
                        R.drawable.chevron_right
                    } else {
                        R.drawable.chevron_left
                    },
                    modifier = Modifier.size(16.dp),
                    colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_secondary.color),
                )
            }
        }
    }
}
