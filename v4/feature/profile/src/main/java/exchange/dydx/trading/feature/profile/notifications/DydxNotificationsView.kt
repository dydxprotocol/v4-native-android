package exchange.dydx.trading.feature.profile.notifications

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
fun Preview_DydxNotificationsView() {
    DydxThemedPreviewSurface {
        DydxNotificationsView.Content(Modifier, SettingsView.ViewState.preview)
    }
}

object DydxNotificationsView : DydxComponent {
    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxNotificationsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: SettingsView.ViewState?) {
        SettingsView.Content(modifier = modifier, state = state)
    }
}
