package exchange.dydx.trading.feature.transfer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.platformui.components.buttons.PlatformPillButton
import exchange.dydx.platformui.components.changes.PlatformAmountChange
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
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold
import exchange.dydx.trading.feature.shared.views.SizeTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import java.lang.Double.max

@Preview
@Composable
fun Preview_TransferAmountBox() {
    DydxThemedPreviewSurface(background = ThemeColor.SemanticColor.layer_3) {
        TransferAmountBox.Content(Modifier, TransferAmountBox.ViewState.preview)
    }
}

object TransferAmountBox {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val parser: ParserProtocol,
        val value: String? = null,
        val placeholder: String? = null,
        val maxAmount: Double? = null,
        val stepSize: Int? = null,
        val tokenText: TokenTextView.ViewState? = null,
        val maxAction: (() -> Unit)? = null,
        val onEditAction: ((String) -> Unit)? = null,
    ) {
        val transferAmount: Double?
            get() {
                if (value == null) {
                    return null
                }
                return parser.asDouble(value)
            }

        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                parser = Parser(),
                placeholder = "0.000",
                maxAmount = 1000.0,
                stepSize = 3,
                tokenText = TokenTextView.ViewState.preview,
                maxAction = {},
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(modifier) {
            InputFieldScaffold(Modifier.zIndex(1f)) {
                TopContent(modifier, state)
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
                BottomContent(modifier, state)
            }
        }
    }

    @Composable
    private fun TopContent(
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
                    value = state.value ?: "",
                    textStyle = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                    placeHolder = if (state.value == null) {
                        state.formatter.raw(0.0, state.stepSize ?: 0)
                    } else {
                        null
                    },
                    onValueChange = { state.onEditAction?.invoke(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            if (state.maxAction != null) {
                PlatformPillButton(action = { state.maxAction.invoke() }) {
                    Text(
                        text = state.localizer.localize("APP.GENERAL.MAX"),
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.mini),
                    )
                }
            }
        }
    }

    @Composable
    private fun BottomContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.AVAILABLE"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            TokenTextView.Content(
                modifier = Modifier,
                state = state.tokenText,
                textStyle = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.mini),
            )

            Spacer(modifier = Modifier.weight(1f))

            AmountChange(modifier = Modifier, state = state)
        }
    }

    @Composable
    private fun AmountChange(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier,
        ) {
            if (state.maxAmount == null || state.maxAmount == 0.0) {
                Text(
                    text = if (state.maxAmount == null) "-" else "0",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
                return
            }
            val maxAmountText = state.formatter.raw(state.maxAmount, 3)
            if (state.transferAmount == null || state.transferAmount == 0.0) {
                Text(
                    text = maxAmountText ?: "0",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            } else {
                PlatformAmountChange(
                    before = {
                        SizeTextView.Content(
                            modifier = Modifier,
                            state = SizeTextView.ViewState(
                                localizer = state.localizer,
                                formatter = state.formatter,
                                size = state.maxAmount,
                                stepSize = state.stepSize,
                            ),
                        )
                    },
                    after = {
                        SizeTextView.Content(
                            modifier = Modifier,
                            state = SizeTextView.ViewState(
                                localizer = state.localizer,
                                formatter = state.formatter,
                                size = max(0.0, state.maxAmount - (state.transferAmount ?: 0.0)),
                                stepSize = state.stepSize,
                            ),
                        )
                    },
                )
            }
        }
    }
}
