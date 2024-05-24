package exchange.dydx.trading.feature.profile.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.feature.shared.views.SettingsView

@Preview
@Composable
fun Preview_DydxSettingsView() {
    DydxThemedPreviewSurface {
        DydxSettingsView.Content(Modifier, SettingsView.ViewState.preview)
    }
}

object DydxSettingsView : DydxComponent {

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxSettingsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: SettingsView.ViewState?) {
        SettingsView.Content(modifier = modifier, state = state)
    }
}
