package exchange.dydx.trading.feature.transfer.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_DydxInstantDepositSearchView() {
    DydxThemedPreviewSurface {
        DydxInstantDepositSearchView.Content(
            Modifier,
            DydxInstantDepositSearchView.ViewState.preview,
        )
    }
}

object DydxInstantDepositSearchView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val backButtonAction: () -> Unit = {},
        val tokens: List<DydxInstantDepositSearchItem.ViewState>?,
        val otherTokens: List<DydxInstantDepositSearchItem.ViewState>?,
        val nobleItem: DydxTransferNobleItemView.ViewState?,
        val fiatItem: DydxTransferFiatItemView.ViewState?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                tokens = listOf(
                    DydxInstantDepositSearchItem.ViewState.preview,
                    DydxInstantDepositSearchItem.ViewState.preview,
                ),
                otherTokens = listOf(
                    DydxInstantDepositSearchItem.ViewState.preview,
                    DydxInstantDepositSearchItem.ViewState.preview,
                ),
                nobleItem = DydxTransferNobleItemView.ViewState.preview,
                fiatItem = DydxTransferFiatItemView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxInstantDepositSearchViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val listState = rememberLazyListState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.GENERAL.SELECT_TOKEN"),
                backAction = state.backButtonAction,
            )

            PlatformDivider()

            LazyColumn(
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                state = listState,
            ) {
                stickyHeader(key = "your-tokens") {
                    Text(
                        text = state.localizer.localize("APP.GENERAL.YOUR_TOKENS"),
                        modifier = Modifier
                            .themeColor(ThemeColor.SemanticColor.layer_2)
                            .fillMaxWidth()
                            .padding(vertical = ThemeShapes.VerticalPadding),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }

                item {
                    DydxTransferNobleItemView.Content(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        state = state.nobleItem,
                    )
                }

                item {
                    DydxTransferFiatItemView.Content(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        state = state.fiatItem,
                    )
                }

                items((state.tokens ?: emptyList()).count()) { index ->
                    val token = state.tokens?.get(index)
                    if (token == null) {
                        return@items
                    }
                    DydxInstantDepositSearchItem.Content(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        state = token,
                    )
                }

                stickyHeader(key = "all-tokens") {
                    Text(
                        text = state.localizer.localize("APP.GENERAL.OTHER_TOKENS"),
                        modifier = Modifier
                            .themeColor(ThemeColor.SemanticColor.layer_2)
                            .fillMaxWidth()
                            .padding(vertical = ThemeShapes.VerticalPadding),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }

                items((state.otherTokens ?: emptyList()).count()) { index ->
                    val token = state.otherTokens?.get(index)
                    if (token == null) {
                        return@items
                    }
                    DydxInstantDepositSearchItem.Content(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        state = token,
                    )
                }
            }
        }
    }
}
