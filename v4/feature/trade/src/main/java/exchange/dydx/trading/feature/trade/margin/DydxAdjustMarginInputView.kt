package exchange.dydx.trading.feature.trade.margin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.PlatformInfoScaffold
import exchange.dydx.platformui.components.buttons.PlatformPillItem
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.tabgroups.PlatformTabGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton

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
        val localizer: LocalizerProtocol,
        val direction: MarginDirection = MarginDirection.Add,
        val percentage: Double?,
        val percentageOptions: List<PercentageOption>,
        val amountText: String?,
        val subaccountReceipt: SubaccountReceipt,
        val positionReceipt: PositionReceipt,
        val error: String?,
        val marginDirectionAction: (() -> Unit) = {},
        val percentageAction: (() -> Unit) = {},
        val action: (() -> Unit) = {},
        val closeAction: (() -> Unit) = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                direction = MarginDirection.Add,
                percentage = 0.5,
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
//            InputAndSubaccountReceipt(
//                modifier = Modifier,
//                state = state,
//            )
//            Spacer(modifier = Modifier.weight(1f))
//            if (state.error == null) {
//                LiquidationPrice(
//                    modifier = Modifier,
//                    state = state,
//                )
//            } else {
//                Error(
//                    modifier = Modifier,
//                    error = state.error,
//                )
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            PositionReceiptAndButton(
//                modifier = Modifier,
//                state = state,
//            )
        }
    }

    @Composable
    fun NavigationHeader(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                modifier = Modifier.padding(horizontal = 0.dp),
                style = TextStyle.dydxDefault
                    .themeFont(
                        fontSize = ThemeFont.FontSize.large,
                        fontType = ThemeFont.FontType.plus,
                    )
                    .themeColor(ThemeColor.SemanticColor.text_primary),
                text = state.localizer.localize("APP.TRADE.ADJUST_ISOLATED_MARGIN"),
            )
            Spacer(modifier = Modifier.weight(1f))
            HeaderViewCloseBotton(
                closeAction = state.closeAction,
            )
        }
    }


    @Composable
    fun MarginDirection(
        modifier: Modifier,
        state: ViewState,
    ) {
        val shape = RoundedCornerShape(10.dp)
        Row(
            modifier = Modifier
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                )
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = 16.dp,
                )
                .clickable { state.marginDirectionAction() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 0.dp),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.medium,
                            fontType = ThemeFont.FontType.book,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                    text = state.localizer.localize("APP.TRADE.ADD_MARGIN"),
                )

                Text(
                    modifier = Modifier
                        .padding(horizontal = 0.dp),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.small,
                            fontType = ThemeFont.FontType.book,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    text = state.localizer.localize("APP.TRADE.REMOVE_MARGIN"),
                )
            }
        }
    }

    @Composable
    fun PercentageOptions(
        modifier: Modifier,
        state: ViewState,
    ) {
        PlatformTabGroup(
            modifier = Modifier.fillMaxWidth(),
            scrollingEnabled = false,
            items = state.percentageOptions.map {
                { modifier ->
                    PlatformPillItem(
                        modifier = Modifier
                            .padding(
                                vertical = 4.dp,
                                horizontal = 8.dp,
                            ),
                        backgroundColor = ThemeColor.SemanticColor.layer_5,
                    ) {
                        Text(
                            text = it.text,
                            modifier = Modifier,
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_tertiary)
                                .themeFont(fontSize = ThemeFont.FontSize.small),

                            )
                    }
                }
            } ?: listOf(),
            selectedItems = state.percentageOptions.map {
                { modifier ->
                    PlatformPillItem(
                        modifier = Modifier
                            .padding(
                                vertical = 4.dp,
                                horizontal = 8.dp,
                            ),
                        backgroundColor = ThemeColor.SemanticColor.layer_2,
                    ) {
                        Text(
                            text = it.text,
                            modifier = Modifier,
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_primary)
                                .themeFont(fontSize = ThemeFont.FontSize.small),

                            )
                    }
                }
            } ?: listOf(),
            equalWeight = false,
            currentSelection = state.percentageOptions.indexOfFirst {
                it.percentage == state.percentage
            },
            onSelectionChanged = {},
        )
    }
}

