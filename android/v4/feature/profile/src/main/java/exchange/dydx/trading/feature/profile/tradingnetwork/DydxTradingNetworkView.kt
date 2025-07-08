package exchange.dydx.trading.feature.profile.tradingnetwork

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.SettingsView

@Preview
@Composable
fun Preview_DydxTradingNetworkView() {
    DydxThemedPreviewSurface {
        DydxTradingNetworkView.Content(Modifier, SettingsView.ViewState.preview)
    }
}

object DydxTradingNetworkView : DydxComponent {
    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradingNetworkViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: SettingsView.ViewState?) {
        SettingsView.Content(modifier = modifier, state = state)
    }
}
