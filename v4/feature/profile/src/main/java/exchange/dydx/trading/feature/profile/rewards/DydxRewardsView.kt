package exchange.dydx.trading.feature.profile.rewards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.profile.components.DydxProfileLaunchIncentivesView
import exchange.dydx.trading.feature.profile.components.DydxProfileRewardsView
import exchange.dydx.trading.feature.profile.components.DydxProfileRewardsViewModel
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_DydxRewardsView() {
    DydxThemedPreviewSurface {
        DydxRewardsView.Content(
            Modifier,
            DydxRewardsView.ViewState.preview,
            DydxProfileRewardsView.ViewState.preview,
            DydxRewardsFaqsView.ViewState.preview,
            DydxRewardsEventsView.ViewState.preview,
        )
    }
}

object DydxRewardsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val backButtonAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxRewardsViewModel = hiltViewModel()
        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val summaryViewModel: DydxProfileRewardsViewModel = hiltViewModel()
        val summarytate =
            summaryViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val faqsViewModel: DydxRewardsFaqsViewModel = hiltViewModel()
        val faqsState = faqsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val rewardsViewModel: DydxRewardsEventsViewModel = hiltViewModel()
        val rewardsState =
            rewardsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        Content(modifier, state, summarytate, faqsState, rewardsState)
    }

    @Composable
    fun Content(
        modifier: Modifier,
        state: DydxRewardsView.ViewState?,
        summarytate: DydxProfileRewardsView.ViewState?,
        faqsState: DydxRewardsFaqsView.ViewState?,
        rewardsState: DydxRewardsEventsView.ViewState?,
    ) {
        if (state == null) return

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        val faqsViewModel: DydxRewardsFaqsViewModel = hiltViewModel()
        val faqsViewState =
            faqsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        val eventsViewModel: DydxRewardsEventsViewModel = hiltViewModel()
        val eventsViewState =
            eventsViewModel.state.collectAsStateWithLifecycle(initialValue = null).value

        Column(
            modifier = modifier
                .themeColor(ThemeColor.SemanticColor.layer_2)
                .fillMaxSize(),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.GENERAL.TRADING_REWARDS"),
                backAction = state.backButtonAction,
            )

            LazyColumn(
                modifier = Modifier,
                userScrollEnabled = true,
                state = listState,
            ) {
                item(key = "launch_incentives") {
                    DydxProfileLaunchIncentivesView.Content(
                        Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                    )
                    Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                }
                item(key = "summary") {
                    DydxProfileRewardsView.Content(
                        modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                        state = summarytate,
                        detailed = true,
                    )

                    Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                }

                DydxRewardsFaqsView.Content(
                    scope = this,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(
                            horizontal = ThemeShapes.HorizontalPadding,
                            vertical = ThemeShapes.VerticalPadding * 3,
                        ),
                    state = faqsViewState,
                )
                item(key = "event_spacer") {
                    Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                }

                DydxRewardsEventsView.Content(
                    scope = this,
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(200.dp)
                        .padding(
                            horizontal = ThemeShapes.HorizontalPadding,
                            vertical = ThemeShapes.VerticalPadding * 3,
                        ),
                    state = eventsViewState,
                )
            }
        }
    }
}
