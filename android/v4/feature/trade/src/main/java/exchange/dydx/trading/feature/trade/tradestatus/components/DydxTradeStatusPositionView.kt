package exchange.dydx.trading.feature.trade.tradestatus.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.components.changes.PlatformAmountChange
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.icons.PlatformRoundImage
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
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SizeTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.utilities.utils.toDp

@Preview
@Composable
fun Preview_DydxTradeStatusPositionView() {
    DydxThemedPreviewSurface {
        DydxTradeStatusPositionView.Content(Modifier, DydxTradeStatusPositionView.ViewState.preview)
    }
}

object DydxTradeStatusPositionView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val tokenUrl: String? = null,
        val token: TokenTextView.ViewState? = null,
        val sideBefore: SideTextView.ViewState? = null,
        val sideAfter: SideTextView.ViewState? = null,
        val sizeBefore: SizeTextView.ViewState? = null,
        val sizeAfter: SizeTextView.ViewState? = null,
        val valueBefore: AmountText.ViewState? = null,
        val valueAfter: AmountText.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                token = TokenTextView.ViewState.preview,
                sideBefore = SideTextView.ViewState.preview,
                sideAfter = SideTextView.ViewState.preview,
                sizeBefore = SizeTextView.ViewState.preview,
                sizeAfter = SizeTextView.ViewState.preview,
                valueBefore = AmountText.ViewState.preview,
                valueAfter = AmountText.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeStatusPositionViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlatformRoundImage(
                    icon = state.tokenUrl,
                    size = 29.dp,
                )

                val symbol = state.token?.symbol
                if (symbol != null) {
                    Text(
                        text = state.localizer.localizeWithParams(
                            path = "APP.TRADE.YOUR_MARKET_POSITION",
                            params = mapOf("MARKET" to symbol),
                        ) ?: "",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_primary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            val shape = RoundedCornerShape(8.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(81.dp)
                    .background(ThemeColor.SemanticColor.layer_3.color, shape)
                    .clip(shape),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PositionChange(modifier = Modifier.padding(16.dp), state = state)

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        PlatformAmountChange(
                            before = {
                                SizeTextView.Content(
                                    modifier = Modifier,
                                    state = state.sizeBefore,
                                    textStyle = TextStyle.dydxDefault
                                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                                )
                            },
                            after = if (state.sizeAfter != null) { {
                                SizeTextView.Content(
                                    modifier = Modifier,
                                    state = state.sizeAfter,
                                    textStyle = TextStyle.dydxDefault
                                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                                        .themeColor(ThemeColor.SemanticColor.text_primary),
                                )
                            } } else {
                                null
                            },
                        )

                        TokenTextView.Content(
                            modifier = Modifier.padding(end = 16.dp),
                            state = state.token,
                            textStyle = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_primary)
                                .themeFont(fontSize = ThemeFont.FontSize.tiny),
                        )
                    }

                    PlatformAmountChange(
                        modifier = Modifier.padding(end = 16.dp),
                        before = {
                            AmountText.Content(
                                modifier = Modifier,
                                state = state.valueBefore,
                                textStyle = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.small)
                                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                            )
                        },
                        after = if (state.valueAfter != null) { {
                            AmountText.Content(
                                modifier = Modifier,
                                state = state.valueAfter,
                                textStyle = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.small)
                                    .themeColor(ThemeColor.SemanticColor.text_secondary),
                            )
                        } } else {
                            null
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun PositionChange(modifier: Modifier, state: ViewState) {
        var beforeViewWidth = remember {
            mutableStateOf(0.dp)
        }

        if (state.sideBefore != null && state.sideAfter != null) {
            Column(
                modifier = modifier,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SideTextView.Content(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                beforeViewWidth.value = coordinates.size.width.toDp
                            },
                        state = state.sideBefore,
                    )
                    PlatformImage(
                        icon = R.drawable.icon_position_change,
                        modifier = Modifier.size(12.dp),
                    )
                }
                SideTextView.Content(
                    modifier = Modifier
                        .offset(x = beforeViewWidth.value - 3.dp, y = (-3).dp),
                    state = state.sideAfter,
                )
            }
        } else if (state.sideBefore != null) {
            SideTextView.Content(modifier = modifier, state = state.sideBefore)
        } else if (state.sideAfter != null) {
            SideTextView.Content(modifier = modifier, state = state.sideAfter)
        }
    }
}
