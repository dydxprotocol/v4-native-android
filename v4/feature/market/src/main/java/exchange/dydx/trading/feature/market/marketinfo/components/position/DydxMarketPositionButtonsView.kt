package exchange.dydx.trading.feature.market.marketinfo.components.position

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer

@Preview
@Composable
fun Preview_DydxMarketPositionButtonsView() {
    DydxThemedPreviewSurface {
        DydxMarketPositionButtonsView.Content(
            Modifier,
            DydxMarketPositionButtonsView.ViewState.preview,
        )
    }
}

object DydxMarketPositionButtonsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val addTriggerAction: (() -> Unit)? = null,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketPositionButtonsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PlatformButton(
                text = state.localizer.localize("APP.TRADE.ADD_TP_SL"),
                state = PlatformButtonState.Secondary,
                modifier = Modifier
                    .padding(vertical = ThemeShapes.VerticalPadding)
                    .weight(1f),
                action = state.addTriggerAction ?: {},
            )

            Spacer(modifier = Modifier.width(ThemeShapes.HorizontalPadding))

            PlatformButton(
                text = state.localizer.localize("APP.TRADE.CLOSE_POSITION"),
                state = PlatformButtonState.Destructive,
                modifier = Modifier
                    .padding(vertical = ThemeShapes.VerticalPadding)
                    .weight(1f),
                action = state.closeAction ?: {},
            )
        }
    }
}
