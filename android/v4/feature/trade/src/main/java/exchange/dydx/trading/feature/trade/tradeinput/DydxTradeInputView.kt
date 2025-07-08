package exchange.dydx.trading.feature.trade.tradeinput

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.DydxReceiptView
import exchange.dydx.trading.feature.receipt.validation.DydxValidationView
import exchange.dydx.trading.feature.trade.orderbook.DydxOrderbookView
import exchange.dydx.trading.feature.trade.orderbook.components.DydxOrderbookGroupView
import exchange.dydx.trading.feature.trade.tradeinput.components.DydxTradeInputCtaButtonView
import exchange.dydx.trading.feature.trade.tradeinput.components.DydxTradeInputMarginButtonsView
import exchange.dydx.trading.feature.trade.tradeinput.components.DydxTradeInputOrderbookToggleView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.execution.DydxTradeInputExecutionView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.goodtil.DydxTradeInputGoodTilView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.leverage.DydxTradeInputLeverageView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.limitprice.DydxTradeInputLimitPriceView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.ordertype.DydxTradeInputOrderTypeView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.postonly.DydxTradeInputPostOnlyView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.reduceonly.DydxTradeInputReduceOnlyView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.side.DydxTradeInputSideView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.size.DydxTradeInputSizeView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.timeinforce.DydxTradeInputTimeInForceView
import exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.triggerprice.DydxTradeInputTriggerPriceView
import exchange.dydx.trading.feature.trade.tradeinput.components.sheettip.DydxTradeSheetTipView

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Preview_DydxTradeInputView() {
    DydxThemedPreviewSurface {
        DydxTradeInputView.Content(
            Modifier,
            DydxTradeInputView.ViewState.preview,
            rememberBottomSheetScaffoldState().bottomSheetState,
        )
    }
}

object DydxTradeInputView : DydxComponent {

    val sheetPeekHeight: Dp = 117.dp

    enum class OrderbookToggleState {
        Open,
        Closed,
    }

    enum class BottomSheetState {
        Hidden,
        Tip,
        Expanded,
    }

    enum class InputField {
        Size,
        Leverage,
        LimitPrice,
        TriggerPrice,
        TrailingPercent,
        TimeInForce,
        Execution,
        GoodTil,
        PostOnly,
        ReduceOnly,
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val inputFields: List<InputField> = listOf(),
        val orderbookToggleState: OrderbookToggleState = OrderbookToggleState.Open,
        val requestedBottomSheetState: BottomSheetState? = null,
        val onRequestedBottomSheetStateCompleted: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                inputFields = listOf(
                    InputField.Size,
                    InputField.Leverage,
                    InputField.LimitPrice,
                    InputField.TriggerPrice,
                    InputField.TrailingPercent,
                    InputField.TimeInForce,
                    InputField.Execution,
                    InputField.GoodTil,
                    InputField.PostOnly,
                    InputField.ReduceOnly,
                ),
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    var bottomSheetState: SheetState? = null

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state, bottomSheetState)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?, bottomSheetState: SheetState? = null) {
        if (state == null || bottomSheetState == null) {
            return
        }

        val sheetState: MutableState<SheetState?> = remember { mutableStateOf(null) }

        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val focusManager = LocalFocusManager.current

        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight - 122.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (sheetState.value?.targetValue == SheetValue.PartiallyExpanded) {
                    focusManager.clearFocus()
                    DydxTradeSheetTipView.Content(Modifier)
                }

                IsolatedMarginButtons(Modifier, state)

                PlatformDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(start = ThemeShapes.HorizontalPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DydxTradeInputOrderbookToggleView.Content(Modifier)
                        if (state.orderbookToggleState == OrderbookToggleState.Open) {
                            DydxOrderbookGroupView.Content(Modifier.padding(start = 12.dp))
                        }
                    }
                    DydxTradeInputOrderTypeView.Content(
                        Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    AnimatedVisibility(
                        modifier = Modifier
                            .animateContentSize()
                            .weight(1f),
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally(),
                        visible = state.orderbookToggleState == OrderbookToggleState.Open,
                    ) {
                        DydxOrderbookView.Content(
                            Modifier
                                .fillMaxWidth(),
                        )
                    }
                    EditPanel(
                        modifier = Modifier
                            .animateContentSize()
                            .weight(1f),
                        state = state,
                    )
                }

                Column {
                    DydxReceiptView.Content(Modifier.offset(y = ThemeShapes.VerticalPadding))
                    DydxTradeInputCtaButtonView.Content(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ThemeShapes.HorizontalPadding)
                            .padding(bottom = ThemeShapes.VerticalPadding * 2),
                    )
                }
            }
        }

        LaunchedEffect(bottomSheetState) {
            snapshotFlow { bottomSheetState }.collect { value ->
                sheetState.value = value
//                when (value.targetValue) {
//                    SheetValue.PartiallyExpanded -> {
//                        focusManager.clearFocus()
//                    }
//                    else -> {
//                    }
//                }
            }
        }

        if (state.requestedBottomSheetState != null) {
            when (state.requestedBottomSheetState) {
                BottomSheetState.Hidden -> {
                }

                BottomSheetState.Tip -> {
                }

                BottomSheetState.Expanded -> {
                    LaunchedEffect(key1 = "expand") {
                        bottomSheetState.expand()
                        state.onRequestedBottomSheetStateCompleted()
                    }
                }
            }
        }

//        LaunchedEffect(key1 = "expand") {
//            sheetState.value?.expand()
//        }
    }

    @Composable
    private fun IsolatedMarginButtons(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = ThemeShapes.HorizontalPadding)
                .height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DydxTradeInputMarginButtonsView.Content(Modifier.weight(1f))
            DydxTradeInputSideView.Content(Modifier.weight(1f))
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun RowScope.EditPanel(modifier: Modifier, state: ViewState) {
        val listState = rememberLazyListState()

        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
            state = listState,
        ) {
            items(items = state.inputFields, key = { it.name }) { inputField ->
                when (inputField) {
                    InputField.Size -> {
                        DydxTradeInputSizeView.Content(Modifier.animateItemPlacement())
                    }

                    InputField.Leverage -> {
                        DydxTradeInputLeverageView.Content(Modifier.animateItemPlacement())
                    }

                    InputField.LimitPrice -> {
                        DydxTradeInputLimitPriceView.Content(Modifier.animateItemPlacement())
                    }

                    InputField.TriggerPrice -> {
                        DydxTradeInputTriggerPriceView.Content(Modifier.animateItemPlacement())
                    }

                    InputField.TrailingPercent -> {
                        // DydxTradeInputTriggerPriceView.Content(Modifier)
                    }

                    InputField.TimeInForce -> {
                        DydxTradeInputTimeInForceView.Content(Modifier.animateItemPlacement())
                    }

                    InputField.Execution -> {
                        DydxTradeInputExecutionView.Content(Modifier.animateItemPlacement())
                    }

                    InputField.GoodTil -> {
                        DydxTradeInputGoodTilView.Content(Modifier.animateItemPlacement())
                    }

                    InputField.PostOnly -> {
                        DydxTradeInputPostOnlyView.Content(Modifier.animateItemPlacement())
                    }

                    InputField.ReduceOnly -> {
                        DydxTradeInputReduceOnlyView.Content(Modifier.animateItemPlacement())
                    }
                }
            }

            item {
                DydxValidationView.Content(Modifier.animateItemPlacement())
            }
        }
    }
}
