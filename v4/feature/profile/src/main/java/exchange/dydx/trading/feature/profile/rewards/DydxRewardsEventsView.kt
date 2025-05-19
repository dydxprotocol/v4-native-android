package exchange.dydx.trading.feature.profile.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxRewardsEventsView() {
    DydxThemedPreviewSurface {
        DydxRewardsEventsView.Content(Modifier, DydxRewardsEventsView.ViewState.preview)
    }
}

object DydxRewardsEventsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val title: String?,
        val periods: List<String>,
        val selectedIndex: Int,
        val rewards: List<DydxRewardsEventItemView.ViewState>,
        val onPeriodChanged: (Int) -> Unit,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                title = "Reward History",
                periods = listOf("Daily", "Weekly", "Monthly"),
                selectedIndex = 0,
                rewards = listOf(),
                onPeriodChanged = {},
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxRewardsEventsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier
                .background(
                    color = exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(14.dp),
                ),
        ) {
            DydxRewardsEventsHeaderView.Content(
                modifier = Modifier
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                state = DydxRewardsEventsHeaderView.ViewState(
                    localizer = state.localizer,
                    periods = state.periods,
                    selectedIndex = state.selectedIndex,
                    onPeriodChanged = state.onPeriodChanged,
                ),
            )

            LazyColumn {
                ListContent(
                    this,
                    Modifier
                        .padding(
                            horizontal = ThemeShapes.HorizontalPadding,
                            vertical = ThemeShapes.VerticalPadding,
                        ),
                    state,
                )
            }
        }
    }

    fun Content(scope: LazyListScope, modifier: Modifier, state: ViewState?) {
        if (state == null) return

        scope.item("event_header") {
            DydxRewardsEventsHeaderView.Content(
                modifier = Modifier
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                state = DydxRewardsEventsHeaderView.ViewState(
                    localizer = state.localizer,
                    periods = state.periods,
                    selectedIndex = state.selectedIndex,
                    onPeriodChanged = state.onPeriodChanged,
                ),
            )
        }
        ListContent(
            scope,
            Modifier
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                ),
            state,
        )
    }

    private fun ListContent(scope: LazyListScope, modifier: Modifier, state: ViewState) {
        scope.items(items = state.rewards, key = { it.timeText }) { faq ->
            DydxRewardsEventItemView.ListContent(
                modifier = modifier,
                state = faq,
            )
        }
    }
}
