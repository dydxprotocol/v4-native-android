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
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxRewardsFaqsView() {
    DydxThemedPreviewSurface {
        DydxRewardsFaqsView.Content(Modifier, DydxRewardsFaqsView.ViewState.preview)
    }
}

object DydxRewardsFaqsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val title: DydxRewardsFaqsHeaderView.ViewState,
        val faqs: List<DydxRewardsFaqItemView.ViewState>,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                title = DydxRewardsFaqsHeaderView.ViewState(
                    localizer = MockLocalizer(),
                    title = "FAQs",
                    learnMoreText = "Learn more",
                ),
                faqs = listOf(
                    DydxRewardsFaqItemView.ViewState(
                        localizer = MockLocalizer(),
                        question = "Question",
                        answer = "Answer",
                        expanded = true,
                    ),
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxRewardsFaqsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: DydxRewardsFaqsView.ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(14.dp),
                ),
        ) {
            DydxRewardsFaqsHeaderView.Content(
                modifier = Modifier
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                state = state.title,
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

    fun Content(scope: LazyListScope, modifier: Modifier, state: DydxRewardsFaqsView.ViewState?) {
        if (state == null) return

        scope.item("faq_header") {
            DydxRewardsFaqsHeaderView.Content(
                modifier = Modifier
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                state = state.title,
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
        scope.items(items = state.faqs, key = { it.question }) { faq ->
            DydxRewardsFaqItemView.ListContent(
                modifier = modifier,
                state = faq,
            )
        }
    }
}
