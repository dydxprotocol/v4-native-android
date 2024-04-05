package exchange.dydx.trading.feature.trade.margin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.platformui.components.PlatformInfoScaffold
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputMarginModeView
import exchange.dydx.trading.feature.trade.tradeinput.DydxTradeInputMarginModeViewModel

@Preview
@Composable
fun Preview_DydxAdjustMarginInputView() {
    DydxThemedPreviewSurface {
        DydxAdjustMarginInputView.Content(Modifier, DydxAdjustMarginInputView.ViewState.preview)
    }
}

object DydxAdjustMarginInputView : DydxComponent {
    enum class MarginDirection {
        Add,
        Remove,
    }

    data class PercentageOption(
        val text: String,
        val percentage: Double,
    )

    data class SubaccountReceipt(
        val freeCollateral: List<String>,
        val marginUsage: List<String>,
    )

    data class PositionReceipt(
        val freeCollateral: List<String>,
        val leverage: List<String>,
        val liquidationPrice: List<String>,
    )

    data class ViewState(
        val direction: MarginDirection = MarginDirection.Add,
        val percentageText: String?,
        val percentageOptions: List<PercentageOption>,
        val amountText: String?,
        val subaccountReceipt: SubaccountReceipt,
        val positionReceipt: PositionReceipt,
        val error: String?,
    ) {
        companion object {
            val preview = ViewState(
                direction = MarginDirection.Add,
                percentageText = "50%",
                percentageOptions = listOf(
                    PercentageOption("10%", 0.1),
                    PercentageOption("20%", 0.2),
                    PercentageOption("30%", 0.3),
                    PercentageOption("50%", 0.5),
                ),
                amountText = "500",
                subaccountReceipt = SubaccountReceipt(
                    freeCollateral = listOf("1000.00", "500.00"),
                    marginUsage = listOf("19.34", "38.45"),
                ),
                positionReceipt = PositionReceipt(
                    freeCollateral = listOf("1000.00", "1500.00"),
                    leverage = listOf("3.1", "2.4"),
                    liquidationPrice = listOf("1200.00", "1000.00"),
                ),
                error = null,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAdjustMarginInputViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        PlatformInfoScaffold(modifier = modifier, platformInfo = viewModel.platformInfo) {
            Content(modifier, state)
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .animateContentSize()
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_4),
        ) {
            NavigationHeader(
                modifier = Modifier,
                state = state
            )
            PlatformDivider()
            // Add to remove margin
            MarginDirection(
                modifier = Modifier,
                state = state,
            )
            Spacer(modifier = Modifier.height(8.dp))
            PercentageOptions(
                modifier = Modifier,
                state = state,
            )
            Spacer(modifier = Modifier.height(8.dp))
            InputAndSubaccountReceipt(
                modifier = Modifier,
                state = state,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (state.error == null) {
                LiquidationPrice(
                    modifier = Modifier,
                    state = state,
                )
            } else {
                Error(
                    modifier = Modifier,
                    error = state.error,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            PositionReceiptAndButton(
                modifier = Modifier,
                state = state,
            )
        }
    }
}

