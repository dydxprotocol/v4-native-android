package exchange.dydx.trading.feature.profile.systemstatus

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.bottombar.DydxBottomBarScaffold

@Preview
@Composable
fun Preview_DydxSystemStatusView() {
    DydxThemedPreviewSurface {
        DydxSystemStatusView.Content(Modifier, DydxSystemStatusView.ViewState.preview)
    }
}

object DydxSystemStatusView : DydxComponent {
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
        val viewModel: DydxSystemStatusViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        DydxBottomBarScaffold(Modifier) {
            Content(it, state)
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
    }
}
