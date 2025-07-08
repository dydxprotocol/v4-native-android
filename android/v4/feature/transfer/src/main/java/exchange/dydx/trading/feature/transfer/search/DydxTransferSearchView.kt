package exchange.dydx.trading.feature.transfer.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.inputs.PlatformTextInput
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton

@Preview
@Composable
fun Preview_DydxTransferSearchView() {
    DydxThemedPreviewSurface {
        DydxTransferSearchView.Content(Modifier, DydxTransferSearchView.ViewState.preview)
    }
}

object DydxTransferSearchView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val items: List<DydxTransferSearchItem.ViewState> = emptyList(),
        val searchText: String = "",
        val closeAction: (() -> Unit)? = null,
        val searchTextChanged: ((String) -> Unit)? = null,
        val itemSelected: ((String) -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                items = listOf(
                    DydxTransferSearchItem.ViewState.preview,
                    DydxTransferSearchItem.ViewState.preview,
                    DydxTransferSearchItem.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferSearchViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @OptIn(ExperimentalFoundationApi::class)
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
                modifier = Modifier,
                state = state,
            )

            PlatformDivider()

            if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ThemeShapes.HorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (state.searchText.isEmpty()) {
                            state.localizer.localize("APP.GENERAL.START_SEARCH")
                        } else {
                            state.localizer.localize("APP.GENERAL.NO_RESULTS")
                        },
                        style = TextStyle.dydxDefault
                            .themeFont(
                                fontSize = ThemeFont.FontSize.medium,
                            ),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                ) {
                    itemsIndexed(state.items) { index, item ->
                        Column(
                            Modifier
                                .clickable {
                                    state.itemSelected?.invoke(item.id)
                                }
                                .animateItemPlacement(),
                        ) {
                            if (item == state.items.first()) {
                                PlatformDivider()
                            }
                            DydxTransferSearchItem.Content(
                                modifier = Modifier,
                                state = item,
                            )
                            if (item != state.items.last()) {
                                PlatformDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun HeaderView(
        modifier: Modifier,
        state: ViewState,
    ) {
        val shape = RoundedCornerShape(50)

        Row(
            modifier = modifier.fillMaxWidth()
                .padding(start = ThemeShapes.HorizontalPadding)
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.weight(1f)
                    .background(exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_4.color, shape = shape)
                    .clip(shape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                PlatformTextInput(
                    placeHolder = state.localizer.localize("APP.GENERAL.SEARCH"),
                    textStyle = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.medium,
                        ),
                    onValueChange = state.searchTextChanged ?: {},
                )
            }
            HeaderViewCloseBotton(closeAction = state.closeAction)
        }
    }
}
