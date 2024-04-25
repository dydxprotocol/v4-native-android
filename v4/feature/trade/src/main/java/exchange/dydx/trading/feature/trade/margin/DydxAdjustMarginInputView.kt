package exchange.dydx.trading.feature.trade.margin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.PlatformInfoScaffold
import exchange.dydx.platformui.components.buttons.PlatformPillItem
import exchange.dydx.platformui.components.changes.PlatformAmountChange
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.inputs.PlatformTextInput
import exchange.dydx.platformui.components.tabgroups.PlatformTabGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton
import exchange.dydx.trading.feature.shared.views.SizeTextView

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
        val formatter: DydxFormatter,
        val direction: MarginDirection = MarginDirection.Add,
        val percentage: Double?,
        val percentageOptions: List<PercentageOption>,
        val amountText: String?,
        val subaccountReceipt: SubaccountReceipt,
        val positionReceipt: PositionReceipt,
        val error: String?,
        val marginDirectionAction: ((direction: MarginDirection) -> Unit) = {},
        val percentageAction: (() -> Unit) = {},
        val editAction: ((String) -> Unit) = {},
        val action: (() -> Unit) = {},
        val closeAction: (() -> Unit) = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
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
                .themeColor(ThemeColor.SemanticColor.layer_4)
                .padding(horizontal = 16.dp),
        ) {
            NavigationHeader(
                modifier = Modifier,
                state = state,
            )
            PlatformDivider()
            Spacer(modifier = Modifier.height(16.dp))
            MarginDirection(
                modifier = Modifier,
                state = state,
            )
            Spacer(modifier = Modifier.height(16.dp))
            PercentageOptions(
                modifier = Modifier,
                state = state,
            )
            Spacer(modifier = Modifier.height(16.dp))
            InputAndSubaccountReceipt(
                modifier = Modifier,
                state = state,
            )
            Spacer(modifier = Modifier.weight(1f))
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

    private fun marginDirectionText(direction: MarginDirection, localizer: LocalizerProtocol): String {
        return when (direction) {
            MarginDirection.Add -> localizer.localize("APP.TRADE.ADD_MARGIN")
            MarginDirection.Remove -> localizer.localize("APP.TRADE.REMOVE_MARGIN")
        }
    }

    @Composable
    fun MarginDirection(
        modifier: Modifier,
        state: ViewState,
    ) {
        val directions = listOf(MarginDirection.Add, MarginDirection.Remove)

        PlatformTabGroup(
            modifier = modifier
                .fillMaxWidth()
                .height(42.dp),
            scrollingEnabled = false,
            items = directions.map {
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
                            text = marginDirectionText(it, state.localizer),
                            modifier = Modifier,
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_tertiary)
                                .themeFont(fontSize = ThemeFont.FontSize.small),

                        )
                    }
                }
            },
            selectedItems = directions.map {
                { modifier ->
                    PlatformPillItem(
                        modifier = Modifier
                            .padding(
                                vertical = 4.dp,
                                horizontal = 8.dp,
                            ),
                        backgroundColor = ThemeColor.SemanticColor.layer_2,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = marginDirectionText(it, state.localizer),
                                modifier = Modifier,
                                style = TextStyle.dydxDefault
                                    .themeColor(ThemeColor.SemanticColor.text_primary)
                                    .themeFont(fontSize = ThemeFont.FontSize.small),
                            )
                        }
                    }
                }
            },
            currentSelection = if (state.direction == MarginDirection.Add) 0 else 1,
            onSelectionChanged = { it ->
                state.marginDirectionAction.invoke(directions[it])
            },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        )
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

    @Composable
    fun InputAndSubaccountReceipt(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column {
            InputFieldScaffold(modifier.zIndex(1f)) {
                AmountBox(modifier, state)
            }
            val shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp)
            Column(
                modifier = modifier
                    .offset(y = (-4).dp)
                    .background(color = ThemeColor.SemanticColor.layer_1.color, shape = shape)
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = ThemeShapes.VerticalPadding)
                    .padding(top = 4.dp),
            ) {
                CrossFreeCollateralContent(modifier = Modifier, state)
                CrossMarginContent(modifier = Modifier, state)
            }
        }
    }

    @Composable
    private fun AmountBox(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = ThemeShapes.VerticalPadding),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.AMOUNT"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.mini),
                )

                PlatformTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.amountText ?: "",
                    textStyle = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                    placeHolder = if (state.amountText == null) {
                        state.formatter.raw(0.0, 2)
                    } else {
                        null
                    },
                    onValueChange = { state.editAction.invoke(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
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
                before = {
                    SizeTextView.Content(
                        modifier = Modifier,
                        state = SizeTextView.ViewState(
                            localizer = state.localizer,
                            formatter = state.formatter,
                            size = state.subaccountReceipt.freeCollateral.firstOrNull()?.toDoubleOrNull(),
                            stepSize = 2,
                        ),
                    )
                },
                after = {
                    SizeTextView.Content(
                        modifier = Modifier,
                        state = SizeTextView.ViewState(
                            localizer = state.localizer,
                            formatter = state.formatter,
                            size = state.subaccountReceipt.freeCollateral.lastOrNull()?.toDoubleOrNull(),
                            stepSize = 2,
                        ),
                    )
                },
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
                before = {
                    SizeTextView.Content(
                        modifier = Modifier,
                        state = SizeTextView.ViewState(
                            localizer = state.localizer,
                            formatter = state.formatter,
                            size = state.subaccountReceipt.marginUsage.firstOrNull()?.toDoubleOrNull(),
                            stepSize = 2,
                        ),
                    )
                },
                after = {
                    SizeTextView.Content(
                        modifier = Modifier,
                        state = SizeTextView.ViewState(
                            localizer = state.localizer,
                            formatter = state.formatter,
                            size = state.subaccountReceipt.marginUsage.lastOrNull()?.toDoubleOrNull(),
                            stepSize = 2,
                        ),
                    )
                },
            )
        }
    }
}
