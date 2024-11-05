package exchange.dydx.trading.feature.receipt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptBuyingPowerView
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptFreeCollateralView
import exchange.dydx.trading.feature.receipt.components.equity.DydxReceiptEquityView
import exchange.dydx.trading.feature.receipt.components.exchangerate.DydxReceiptExchangeRateView
import exchange.dydx.trading.feature.receipt.components.exchangereceived.DydxReceiptExchangeReceivedView
import exchange.dydx.trading.feature.receipt.components.expectedprice.DydxReceiptExpectedPriceView
import exchange.dydx.trading.feature.receipt.components.fee.DydxReceiptBridgeFeeView
import exchange.dydx.trading.feature.receipt.components.fee.DydxReceiptFeeView
import exchange.dydx.trading.feature.receipt.components.fee.DydxReceiptGasFeeView
import exchange.dydx.trading.feature.receipt.components.fee.DydxReceiptTransferFeeView
import exchange.dydx.trading.feature.receipt.components.isolatedmargin.DydxReceiptIsolatedPositionMarginUsageView
import exchange.dydx.trading.feature.receipt.components.leverage.DydxReceiptPositionLeverageView
import exchange.dydx.trading.feature.receipt.components.liquidationprice.DydxReceiptLiquidationPriceView
import exchange.dydx.trading.feature.receipt.components.marginusage.DydxReceiptMarginUsageView
import exchange.dydx.trading.feature.receipt.components.rewards.DydxReceiptRewardsView
import exchange.dydx.trading.feature.receipt.components.slippage.DydxReceiptSlippageView
import exchange.dydx.trading.feature.receipt.components.transferduration.DydxReceiptTransferDurationView

@Preview
@Composable
fun Preview_DydxReceiptView() {
    DydxThemedPreviewSurface {
        DydxReceiptView.Content(Modifier, DydxReceiptView.ViewState.preview)
    }
}

object DydxReceiptView : DydxComponent {
    enum class ReceiptLineType {
        FreeCollateral,
        BuyingPower,
        MarginUsage,
        PositionLeverage,
        IsolatedPositionMarginUsage,
        Fee,
        GasFee,
        BridgeFee,
        TransferFee,
        ExpectedPrice,
        Rewards,
        Equity,
        ExchangeRate,
        ExchangeReceived,
        TransferDuration,
        Slippage,
        PositionMargin,
        LiquidationPrice,
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val height: Dp? = null,
        val padding: Dp? = null,
        val lineTypes: List<ReceiptLineType> = emptyList(),
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                lineTypes = listOf(
                    ReceiptLineType.BuyingPower,
                    ReceiptLineType.MarginUsage,
                    ReceiptLineType.Fee,
                    ReceiptLineType.ExpectedPrice,
                    ReceiptLineType.Rewards,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxReceiptViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Box(
            modifier = modifier
                .heightIn(max = state.height ?: 210.dp)
                .fillMaxWidth()
                .padding(horizontal = state.padding ?: ThemeShapes.HorizontalPadding)
                .background(
                    color = ThemeColor.SemanticColor.layer_1.color,
                    shape = RoundedCornerShape(10.dp),
                ),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding * 2,
                    ),
                verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
            ) {
                items(state.lineTypes, key = { it }) { lineType ->
                    when (lineType) {
                        ReceiptLineType.FreeCollateral -> {
                            DydxReceiptFreeCollateralView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.BuyingPower -> {
                            DydxReceiptBuyingPowerView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.MarginUsage -> {
                            DydxReceiptMarginUsageView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.PositionLeverage -> {
                            DydxReceiptPositionLeverageView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.IsolatedPositionMarginUsage -> {
                            DydxReceiptIsolatedPositionMarginUsageView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.Fee -> {
                            DydxReceiptFeeView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.ExpectedPrice -> {
                            DydxReceiptExpectedPriceView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.Rewards -> {
                            DydxReceiptRewardsView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.Equity -> {
                            DydxReceiptEquityView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.ExchangeRate -> {
                            DydxReceiptExchangeRateView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.ExchangeReceived -> {
                            DydxReceiptExchangeReceivedView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.TransferDuration -> {
                            DydxReceiptTransferDurationView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.Slippage -> {
                            DydxReceiptSlippageView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.GasFee -> {
                            DydxReceiptGasFeeView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.BridgeFee -> {
                            DydxReceiptBridgeFeeView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.PositionMargin -> {
                            DydxReceiptIsolatedPositionMarginUsageView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.LiquidationPrice -> {
                            DydxReceiptLiquidationPriceView.Content(Modifier.animateItemPlacement())
                        }

                        ReceiptLineType.TransferFee -> {
                            DydxReceiptTransferFeeView.Content(Modifier.animateItemPlacement())
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                }
            }
        }
    }
}
