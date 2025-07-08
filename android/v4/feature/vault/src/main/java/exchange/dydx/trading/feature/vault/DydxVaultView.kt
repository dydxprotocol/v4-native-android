package exchange.dydx.trading.feature.vault

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.compose.PlatformRememberLazyListState
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.bottombar.DydxBottomBarScaffold
import exchange.dydx.trading.feature.vault.components.DydxVaultChartView
import exchange.dydx.trading.feature.vault.components.DydxVaultHeaderView
import exchange.dydx.trading.feature.vault.components.DydxVaultInfoView
import exchange.dydx.trading.feature.vault.components.DydxVaultPositionItemView
import exchange.dydx.trading.feature.vault.components.DydxVaultPositionsHeaderView
import kotlinx.coroutines.launch

@Preview
@Composable
fun Preview_DydxVaultView() {
    DydxThemedPreviewSurface {
        DydxVaultView.Content(Modifier, DydxVaultView.ViewState.preview)
    }
}

object DydxVaultView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val positionCount: Int = 0,
        val items: List<DydxVaultPositionItemView.ViewState> = listOf(),
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    private var firstTime = true

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        DydxBottomBarScaffold(Modifier) {
            Content(it, state)
        }
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
            DydxVaultHeaderView.Content(modifier = Modifier)

            ScrollingContent(modifier = Modifier.weight(1f), state = state)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ScrollingContent(modifier: Modifier, state: ViewState) {
        val listState = PlatformRememberLazyListState(key = "ScrollingContent")
        val scope = rememberCoroutineScope()

        LazyColumn(
            modifier = modifier,
            state = listState,
        ) {
            item(key = "info") {
                DydxVaultInfoView.Content(Modifier)
            }
            item(key = "chart") {
                DydxVaultChartView.Content(Modifier)
                PlatformDivider()
            }
            stickyHeader(key = "positions_header") {
                DydxVaultPositionsHeaderView.Content(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .themeColor(ThemeColor.SemanticColor.layer_2),
                    state = DydxVaultPositionsHeaderView.ViewState(
                        localizer = state.localizer,
                        positionCount = state.items.count(),
                    ),
                )
            }
            items(items = state.items, key = { it.id }) { item ->
                DydxVaultPositionItemView.Content(Modifier, item)
                if (state.items.last() !== item) {
                    PlatformDivider()
                }
            }

            if (firstTime) {
                firstTime = false
                scope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }
}
