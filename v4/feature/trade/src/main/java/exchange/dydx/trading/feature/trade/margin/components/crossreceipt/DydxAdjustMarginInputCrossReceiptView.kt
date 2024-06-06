package exchange.dydx.trading.feature.trade.margin.components.crossreceipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.changes.PlatformAmountChange
import exchange.dydx.platformui.components.changes.PlatformDirection
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptFreeCollateralView
import exchange.dydx.trading.feature.receipt.components.marginusage.DydxReceiptMarginUsageView
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.MarginUsageView

@Preview
@Composable
fun Preview_DydxAdjustMarginInputCrossReceiptView() {
    DydxThemedPreviewSurface {
        DydxAdjustMarginInputCrossReceiptView.Content(
            Modifier,
            DydxAdjustMarginInputCrossReceiptView.ViewState.preview,
        )
    }
}

object DydxAdjustMarginInputCrossReceiptView : DydxComponent {
    data class CrossMarginReceipt(
        val freeCollateral: DydxReceiptFreeCollateralView.ViewState,
        val marginUsage: DydxReceiptMarginUsageView.ViewState,
    )

    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val crossMarginReceipt: CrossMarginReceipt,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                crossMarginReceipt = CrossMarginReceipt(
                    freeCollateral = DydxReceiptFreeCollateralView.ViewState.preview,
                    marginUsage = DydxReceiptMarginUsageView.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAdjustMarginInputCrossReceiptViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CrossFreeCollateralContent(modifier = Modifier, state)
            CrossMarginContent(modifier = Modifier, state)
        }
    }

    @Composable
    private fun CrossFreeCollateralContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.CROSS_FREE_COLLATERAL"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )
            Spacer(modifier = Modifier.weight(1f))

            CrossFreeCollateralChange(modifier = Modifier, state = state)
        }
    }

    @Composable
    private fun CrossFreeCollateralChange(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier,
        ) {
            PlatformAmountChange(
                modifier = Modifier.weight(1f),
                before = if (state.crossMarginReceipt.freeCollateral.before != null) {
                    {
                        AmountText.Content(
                            state = state.crossMarginReceipt.freeCollateral.before,
                            textStyle = TextStyle.dydxDefault
                                .themeFont(
                                    fontType = ThemeFont.FontType.number,
                                    fontSize = ThemeFont.FontSize.small,
                                )
                                .themeColor(ThemeColor.SemanticColor.text_tertiary),
                        )
                    }
                } else {
                    null
                },
                after = if (state.crossMarginReceipt.freeCollateral.after != null) {
                    {
                        AmountText.Content(
                            state = state.crossMarginReceipt.freeCollateral.after,
                            textStyle = TextStyle.dydxDefault
                                .themeFont(
                                    fontType = ThemeFont.FontType.number,
                                    fontSize = ThemeFont.FontSize.small,
                                )
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    }
                } else {
                    null
                },
                direction = PlatformDirection.from(
                    state.crossMarginReceipt.freeCollateral.before?.amount,
                    state.crossMarginReceipt.freeCollateral.after?.amount,
                ),
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }

    @Composable
    private fun CrossMarginContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.CROSS_MARGIN_USAGE"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )
            Spacer(modifier = Modifier.weight(1f))

            CrossMarginUsageChange(modifier = Modifier, state = state)
        }
    }

    @Composable
    private fun CrossMarginUsageChange(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier,
        ) {
            PlatformAmountChange(
                modifier = Modifier.weight(1f),
                before = if (state.crossMarginReceipt.marginUsage.before != null) {
                    {
                        MarginUsageView.Content(
                            state = state.crossMarginReceipt.marginUsage.before,
                            formatter = state.formatter,
                            textStyle = TextStyle.dydxDefault
                                .themeFont(
                                    fontSize = ThemeFont.FontSize.small,
                                    fontType = ThemeFont.FontType.number,
                                )
                                .themeColor(ThemeColor.SemanticColor.text_tertiary),
                        )
                    }
                } else {
                    null
                },
                after = if (state.crossMarginReceipt.marginUsage.after != null) {
                    {
                        MarginUsageView.Content(
                            state = state.crossMarginReceipt.marginUsage.after,
                            formatter = state.formatter,
                            textStyle = TextStyle.dydxDefault
                                .themeFont(
                                    fontSize = ThemeFont.FontSize.small,
                                    fontType = ThemeFont.FontType.number,
                                )
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    }
                } else {
                    null
                },
                direction = PlatformDirection.from(
                    state.crossMarginReceipt.marginUsage.after?.percent,
                    state.crossMarginReceipt.marginUsage.before?.percent,
                ),
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }
}
