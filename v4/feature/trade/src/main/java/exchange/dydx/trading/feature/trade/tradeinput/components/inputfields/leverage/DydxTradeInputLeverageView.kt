package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.leverage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.inputs.PlatformTextInput
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold
import exchange.dydx.trading.feature.shared.views.GradientSlider
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.utilities.utils.rounded
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Preview
@Composable
fun Preview_DydxTradeInputLeverageView() {
    DydxThemedPreviewSurface {
        DydxTradeInputLeverageView.Content(Modifier, DydxTradeInputLeverageView.ViewState.preview)
    }
}

object DydxTradeInputLeverageView : DydxComponent {

    enum class OrderSide {
        Buy, Sell;
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val leverage: Double?,
        val positionLeverage: Double?,
        val maxLeverage: Double?,
        val side: OrderSide = OrderSide.Buy,
        val sideToggleAction: (OrderSide) -> Unit = {},
        val leverageUpdateAction: (String) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                leverage = -5.0,
                positionLeverage = -1.0,
                maxLeverage = 10.0,
                side = OrderSide.Sell,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputLeverageViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        InputFieldScaffold(modifier) {
            Column(
                modifier = modifier
                    .padding(ThemeShapes.InputPaddingValues)
                    .heightIn(min = ThemeShapes.InputHeight),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                HeaderContent(modifier = Modifier, state = state)
                SliderContent(modifier = Modifier, state = state)
            }
        }
    }

    @Composable
    private fun HeaderContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier.padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.LEVERAGE"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )

                TextInputContent(
                    modifier = Modifier.width(80.dp),
                    state = state,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            SideToggleContent(
                modifier = Modifier,
                state = state,
            )
        }
    }

    @Composable
    private fun TextInputContent(modifier: Modifier, state: ViewState) {
        val leverageValue = state.formatter.raw(abs(state.leverage ?: 0.0), 1)
        PlatformTextInput(
            modifier = modifier,
            value = leverageValue,
            onValueChange = {
                state.leverageUpdateAction(it)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }

    @Composable
    private fun SideToggleContent(modifier: Modifier, state: ViewState) {
        val focusManager = LocalFocusManager.current
        val shape = RoundedCornerShape(4.dp)
        Column(
            modifier = modifier
                .clickable {
                    state.sideToggleAction(state.side)
                    focusManager.clearFocus()
                }
                .background(
                    color = when (state.side) {
                        OrderSide.Buy -> ThemeColor.SemanticColor.positiveColor.color
                        OrderSide.Sell -> ThemeColor.SemanticColor.negativeColor.color
                    }.copy(alpha = 0.1f),
                    shape = shape,
                )
                .border(
                    width = 1.dp,
                    color = when (state.side) {
                        OrderSide.Buy -> ThemeColor.SemanticColor.positiveColor.color
                        OrderSide.Sell -> ThemeColor.SemanticColor.negativeColor.color
                    }.copy(alpha = 0.6f),
                    shape = shape,
                )
                .padding(horizontal = 4.dp)
                .padding(vertical = 2.dp),
        ) {
            SideTextView.Content(
                modifier = Modifier,
                state = SideTextView.ViewState(
                    localizer = state.localizer,
                    coloringOption = SideTextView.ColoringOption.COLORED,
                    side = when (state.side) {
                        OrderSide.Buy -> SideTextView.Side.Long
                        OrderSide.Sell -> SideTextView.Side.Short
                    },
                ),
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini),
            )
        }
    }

    @Composable
    private fun SliderContent(modifier: Modifier, state: ViewState) {
        val focusManager = LocalFocusManager.current

        val leverage = state.leverage?.toFloat() ?: 0f
        val maxLeverage = state.maxLeverage?.toFloat() ?: return
        val positionLeverage = state.positionLeverage?.toFloat() ?: 0f

        if (maxLeverage == 0f) {
            return
        }

        val value = (positionLeverage / maxLeverage).rounded(toPlaces = 1)
        val positionRatio = min(1f, max(-1f, value))

        val sliderViewState = GradientSlider.ViewState(
            localizer = state.localizer,
            value = leverage,
            valueRange = when (state.side) {
                OrderSide.Sell -> -maxLeverage..positionLeverage
                OrderSide.Buy -> positionLeverage..maxLeverage
            },
            onValueChange = {
                state.formatter.raw(it.toDouble())?.let { stringValue ->
                    state.leverageUpdateAction(stringValue)
                }
                focusManager.clearFocus()
            },
            leftRatio = when (state.side) {
                OrderSide.Sell -> -1f
                OrderSide.Buy -> positionRatio
            },
            rightRatio = when (state.side) {
                OrderSide.Sell -> positionRatio
                OrderSide.Buy -> 1f
            },
        )

        val absPositionLeverage = abs(positionLeverage).rounded(1)
        val absMaxLeverage = abs(maxLeverage).rounded(1)

        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            GradientSlider.Content(modifier, sliderViewState)
            if (sliderViewState.leftRatio < sliderViewState.rightRatio) {
                Row {
                    Text(
                        text = when (state.side) {
                            OrderSide.Sell -> "${absMaxLeverage}x"
                            OrderSide.Buy -> "${absPositionLeverage}x"
                        },
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.tiny),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = when (state.side) {
                            OrderSide.Sell -> "${absPositionLeverage}x"
                            OrderSide.Buy -> "${absMaxLeverage}x"
                        },
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.tiny),
                    )
                }
            }
        }
    }
}
