package exchange.dydx.trading.feature.profile.actions.debugenabled

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.profile.settings.DydxSettingsView

@Preview
@Composable
fun Preview_DydxDebugEnableView() {
    DydxThemedPreviewSurface {
        DydxDebugEnableView.Content(Modifier, DydxDebugEnableView.ViewState.preview)
    }
}

object DydxDebugEnableView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxDebugEnableViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
        DydxSettingsView.Content(modifier)
    }
}
