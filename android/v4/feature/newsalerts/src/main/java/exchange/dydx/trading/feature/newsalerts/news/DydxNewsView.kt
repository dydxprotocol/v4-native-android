package exchange.dydx.trading.feature.newsalerts.news

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.PlatformWebView
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxNewsView() {
    DydxThemedPreviewSurface {
        DydxNewsView.Content(Modifier, DydxNewsView.ViewState.preview)
    }
}

object DydxNewsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val url: String?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                url = "https://www.google.com",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxNewsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state?.url == null) {
            return
        }

        PlatformWebView(
            modifier = modifier,
            url = state.url,
        )
    }
}
