package exchange.dydx.trading.feature.trade.closeposition

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.DydxReceiptView
import exchange.dydx.trading.feature.receipt.validation.DydxValidationView
import exchange.dydx.trading.feature.trade.closeposition.components.DydxClosePositionHeaderView
import exchange.dydx.trading.feature.trade.closeposition.components.DydxClosePositionInputCtaButtonView
import exchange.dydx.trading.feature.trade.closeposition.components.DydxClosePositionInputPercentView
import exchange.dydx.trading.feature.trade.closeposition.components.DydxClosePositionInputSizeView
import exchange.dydx.trading.feature.trade.orderbook.components.DydxOrderbookGroupView
import exchange.dydx.trading.feature.trade.orderbook.components.DydxOrderbookSideView
import exchange.dydx.trading.feature.trade.orderbook.components.DydxOrderbookSpreadView

@Preview
@Composable
fun Preview_DydxClosePositionInputView() {
    DydxThemedPreviewSurface {
        DydxClosePositionInputView.Content(Modifier, DydxClosePositionInputView.ViewState.preview)
    }
}

object DydxClosePositionInputView : DydxComponent {
    enum class DisplaySide {
        Asks, Bids, None,
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val side: DisplaySide,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                side = DisplaySide.Asks,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxClosePositionInputViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val focusManager = LocalFocusManager.current

        Column(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
        ) {
            DydxClosePositionHeaderView.Content(Modifier)

            PlatformDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = ThemeShapes.VerticalPadding * 2),
                horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding * 2),
                ) {
                    DydxOrderbookGroupView.Content(
                        Modifier
                            .padding(start = ThemeShapes.HorizontalPadding),
                    )
                    when (state.side) {
                        DisplaySide.Asks -> {
                            DydxOrderbookSideView.AsksContent(
                                modifier = Modifier
                                    .height(240.dp),
                            )
                        }
                        DisplaySide.Bids -> {
                            DydxOrderbookSideView.BidsContent(
                                modifier = Modifier
                                    .height(240.dp),
                            )
                        }
                        DisplaySide.None -> {}
                    }
                    DydxOrderbookSpreadView.Content(
                        Modifier
                            .padding(start = ThemeShapes.HorizontalPadding),
                    )
                }

                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = ThemeShapes.HorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding * 2),
                ) {
                    item {
                        DydxClosePositionInputSizeView.Content(Modifier.animateItemPlacement())
                    }
                    item {
                        DydxClosePositionInputPercentView.Content(Modifier.animateItemPlacement())
                    }
                    item {
                        DydxValidationView.Content(Modifier.animateItemPlacement())
                    }
                }
            }

            //  Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                DydxReceiptView.Content(Modifier.offset(y = ThemeShapes.VerticalPadding))
                DydxClosePositionInputCtaButtonView.Content(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(bottom = ThemeShapes.VerticalPadding * 2),
                )
            }
        }
    }
}
