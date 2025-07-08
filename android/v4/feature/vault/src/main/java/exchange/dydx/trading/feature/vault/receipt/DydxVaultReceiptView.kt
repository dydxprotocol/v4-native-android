package exchange.dydx.trading.feature.vault.receipt

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.components.DydxReceiptItemView
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptFreeCollateralView
import exchange.dydx.trading.feature.receipt.components.marginusage.DydxReceiptMarginUsageView
import exchange.dydx.trading.feature.receipt.components.slippage.DydxReceiptSlippageView

@Preview
@Composable
fun Preview_DydxVaultReceiptView() {
    DydxThemedPreviewSurface {
        DydxVaultReceiptView.Content(Modifier, DydxVaultReceiptView.ViewState.preview)
    }
}

object DydxVaultReceiptView : DydxComponent {
    enum class VaultReceiptLineType {
        FreeCollateral,
        MarginUsage,
        Balance,
        Slippage,
        AmountReceived,
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val lineTypes: List<VaultReceiptLineType> = emptyList(),
        val freeCollateral: DydxReceiptFreeCollateralView.ViewState? = null,
        val marginUsage: DydxReceiptMarginUsageView.ViewState? = null,
        val slippage: DydxReceiptItemView.ViewState? = null,
        val balance: DydxReceiptFreeCollateralView.ViewState? = null,
        val amountReceived: DydxReceiptItemView.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                lineTypes = listOf(
                    VaultReceiptLineType.FreeCollateral,
                    VaultReceiptLineType.MarginUsage,
                    VaultReceiptLineType.Balance,
                ),
                freeCollateral = DydxReceiptFreeCollateralView.ViewState.preview,
                marginUsage = DydxReceiptMarginUsageView.ViewState.preview,
                slippage = DydxReceiptItemView.ViewState.preview,
                balance = DydxReceiptFreeCollateralView.ViewState.preview,
                amountReceived = DydxReceiptItemView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultReceiptViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
        Box(
            modifier = modifier
                .heightIn(max = 210.dp)
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding)
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
                        VaultReceiptLineType.FreeCollateral -> {
                            DydxReceiptFreeCollateralView.Content(
                                modifier = Modifier.animateItemPlacement(),
                                state = state.freeCollateral,
                            )
                        }

                        VaultReceiptLineType.MarginUsage -> {
                            DydxReceiptMarginUsageView.Content(
                                modifier = Modifier.animateItemPlacement(),
                                state = state.marginUsage,
                            )
                        }

                        VaultReceiptLineType.Balance -> {
                            DydxReceiptFreeCollateralView.Content(
                                modifier = Modifier.animateItemPlacement(),
                                state = state.balance,
                            )
                        }

                        VaultReceiptLineType.Slippage -> {
                            DydxReceiptSlippageView.Content(
                                modifier = Modifier.animateItemPlacement(),
                                state = state.slippage,
                            )
                        }

                        VaultReceiptLineType.AmountReceived -> {
                            DydxReceiptItemView.Content(
                                modifier = Modifier.animateItemPlacement(),
                                state = state.amountReceived,
                            )
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
