package exchange.dydx.trading.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.compose.PlatformRememberLazyListState
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.profile.components.DydxProfileAlertsView
import exchange.dydx.trading.feature.profile.components.DydxProfileBalancesView
import exchange.dydx.trading.feature.profile.components.DydxProfileButtonsView
import exchange.dydx.trading.feature.profile.components.DydxProfileFeesView
import exchange.dydx.trading.feature.profile.components.DydxProfileHeaderView
import exchange.dydx.trading.feature.profile.components.DydxProfileHistoryView
import exchange.dydx.trading.feature.profile.components.DydxProfileRewardsView
import exchange.dydx.trading.feature.shared.bottombar.DydxBottomBarScaffold

@Preview
@Composable
fun Preview_DydxProfileView() {
    DydxThemedPreviewSurface {
        DydxProfileView.Content(Modifier, DydxProfileView.ViewState.preview)
    }
}

object DydxProfileView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val hasAlerts: Boolean,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                hasAlerts = true,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxProfileViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        DydxBottomBarScaffold(Modifier) {
            Content(it, state)
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        val listState = PlatformRememberLazyListState(key = "DydxProfileView")

        Box(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            LazyColumn(
                modifier = Modifier,
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "header") {
                    DydxProfileHeaderView.Content(Modifier.padding(horizontal = ThemeShapes.HorizontalPadding))
                }
                item(key = "buttons") {
                    DydxProfileButtonsView.Content(Modifier.padding(horizontal = ThemeShapes.HorizontalPadding))
                }
                if (state?.hasAlerts == true) {
                    item(key = "alerts") {
                        DydxProfileAlertsView.Content(Modifier.padding(horizontal = ThemeShapes.HorizontalPadding))
                    }
                }
                item(key = "balances") {
                    DydxProfileBalancesView.Content(Modifier.padding(horizontal = ThemeShapes.HorizontalPadding))
                }
                item(key = "fees") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = ThemeShapes.HorizontalPadding,
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        DydxProfileFeesView.Content(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        DydxProfileRewardsView.Content(modifier = Modifier.weight(1f))
                    }
                }
                item(key = "history") {
                    DydxProfileHistoryView.Content(Modifier.padding(horizontal = ThemeShapes.HorizontalPadding))
                }
                item(key = "bottom") {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }
}
