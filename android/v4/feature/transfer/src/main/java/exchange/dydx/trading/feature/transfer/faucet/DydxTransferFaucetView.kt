package exchange.dydx.trading.feature.transfer.faucet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_DydxTransferFaucetView() {
    DydxThemedPreviewSurface {
        DydxTransferFaucetView.Content(Modifier, DydxTransferFaucetView.ViewState.preview)
    }
}

object DydxTransferFaucetView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val ctaButtonAction: (() -> Unit)? = null,
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
        val viewModel: DydxTransferFaucetViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            if (state.closeAction != null) {
                HeaderView(
                    title = "Faucet",
                    closeAction = { state.closeAction.invoke() },
                )

                PlatformDivider()
            }

            Spacer(modifier = Modifier.weight(1f))

            PlatformButton(
                text = "Get Funds from Faucet",
                action = {
                    state.ctaButtonAction?.invoke()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}
