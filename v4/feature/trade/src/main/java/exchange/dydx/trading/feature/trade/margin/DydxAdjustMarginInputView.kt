package exchange.dydx.trading.feature.trade.margin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.alerts.PlatformInlineAlert
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.inputs.PlatformTextInput
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
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold
import exchange.dydx.trading.feature.trade.margin.components.crossreceipt.DydxAdjustMarginInputCrossReceiptView
import exchange.dydx.trading.feature.trade.margin.components.cta.DydxAdjustMarginCtaButton
import exchange.dydx.trading.feature.trade.margin.components.header.DydxAdjustMarginInputHeaderView
import exchange.dydx.trading.feature.trade.margin.components.ioslatedreceipt.DydxAdjustMarginInputIsolatedReceiptView
import exchange.dydx.trading.feature.trade.margin.components.liquidationprice.DydxAdjustMarginInputLiquidationPriceView
import exchange.dydx.trading.feature.trade.margin.components.percent.DydxAdjustMarginInputPercentView
import exchange.dydx.trading.feature.trade.margin.components.type.DydxAdjustMarginInputTypeView

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

    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val amountText: String?,
        val direction: MarginDirection = MarginDirection.Add,
        val error: String?,
        val amountEditAction: ((String) -> Unit) = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                amountText = "500",
                error = null,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAdjustMarginInputViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val focusManager = LocalFocusManager.current

        Column(
            modifier = modifier
                .animateContentSize()
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_3)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                DydxAdjustMarginInputHeaderView.Content(
                    modifier = Modifier,
                )

                PlatformDivider()
            }

            DydxAdjustMarginInputTypeView.Content(
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
            )

            DydxAdjustMarginInputPercentView.Content(
                modifier = Modifier,
            )

            InputAndSubaccountReceipt(
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                state = state,
            )

            Spacer(modifier = Modifier.weight(1f))

            if (state.error == null) {
                DydxAdjustMarginInputLiquidationPriceView.Content(
                    modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                )
            } else {
                Error(
                    modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                    error = state.error,
                )
            }

            PositionReceiptAndButton(
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                state = state,
            )
        }
    }

    @Composable
    fun InputAndSubaccountReceipt(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column(modifier = modifier) {
            InputFieldScaffold(Modifier.zIndex(1f)) {
                AmountBox(Modifier, state)
            }
            val shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp)
            val component: DydxComponent = when (state.direction) {
                MarginDirection.Add -> DydxAdjustMarginInputCrossReceiptView
                MarginDirection.Remove -> DydxAdjustMarginInputIsolatedReceiptView
            }
            component.Content(
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .background(color = ThemeColor.SemanticColor.layer_1.color, shape = shape)
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = 12.dp)
                    .padding(top = 8.dp),
            )
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
                    text = state.localizer.localize(
                        when (state.direction) {
                            MarginDirection.Add -> "APP.GENERAL.AMOUNT_TO_ADD"
                            MarginDirection.Remove -> "APP.GENERAL.AMOUNT_TO_REMOVE"
                        },
                    ),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.mini),
                )

                PlatformTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.amountText ?: "",
                    textStyle = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium, fontType = ThemeFont.FontType.number),
                    placeHolder = state.formatter.raw(0.0, 2),
                    onValueChange = { state.amountEditAction.invoke(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }
    }

    @Composable
    private fun Error(
        error: String,
        modifier: Modifier = Modifier,
    ) {
        PlatformInlineAlert(
            text = error,
            level = PlatformInlineAlert.Level.ERROR,
            modifier = modifier,
        )
    }

    @Composable
    private fun PositionReceiptAndButton(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth(),
        ) {
            val shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp)
            val component: DydxComponent = when (state.direction) {
                MarginDirection.Add -> DydxAdjustMarginInputIsolatedReceiptView
                MarginDirection.Remove -> DydxAdjustMarginInputCrossReceiptView
            }
            component.Content(
                modifier = Modifier
                    .offset(y = 8.dp)
                    .background(color = ThemeColor.SemanticColor.layer_1.color, shape = shape)
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = 12.dp)
                    .padding(bottom = 8.dp),
            )

            DydxAdjustMarginCtaButton.Content(
                disable = state.error != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = ThemeShapes.VerticalPadding * 2),
            )
        }
    }
}
