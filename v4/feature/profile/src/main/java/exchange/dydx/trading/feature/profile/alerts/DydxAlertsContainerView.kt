package exchange.dydx.trading.feature.profile.alerts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.newsalerts.alerts.DydxAlertsView
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_DydxAlertsContainerView() {
    DydxThemedPreviewSurface {
        DydxAlertsContainerView.Content(Modifier, DydxAlertsContainerView.ViewState.preview)
    }
}

object DydxAlertsContainerView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val backButtionAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAlertsContainerViewModel = hiltViewModel()

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
            HeaderView(
                title = state.localizer.localize("APP.GENERAL.ALERTS"),
                modifier = Modifier.fillMaxWidth(),
                backAction = state.backButtionAction,
            )

            DydxAlertsView.Content(modifier = Modifier)
        }
    }
}
