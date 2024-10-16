package exchange.dydx.trading.feature.vault.depositwithdraw.components

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
import androidx.compose.ui.platform.LocalFocusManager
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
import exchange.dydx.platformui.components.changes.PlatformDirection
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
import exchange.dydx.trading.feature.shared.views.AmountText

@Preview
@Composable
fun Preview_VaultAmountBox() {
    DydxThemedPreviewSurface(background = ThemeColor.SemanticColor.layer_3) {
        VaultAmountBox.Content(Modifier, VaultAmountBox.ViewState.preview)
    }
}

object VaultAmountBox {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val parser: ParserProtocol,
        val title: String? = null,
        val value: String? = null,
        val placeholder: String? = null,
        val maxAmount: Double? = null,
        val stepSize: Int? = null,
        val maxAction: (() -> Unit)? = null,
        val onEditAction: ((String) -> Unit)? = null,
        val footer: String? = null,
        val footerBefore: AmountText.ViewState? = null,
        val footerAfter: AmountText.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                parser = Parser(),
                placeholder = "0.000",
                maxAmount = 1000.0,
                stepSize = 3,
                maxAction = {},
                footer = "Available",
                footerBefore = AmountText.ViewState.preview,
                footerAfter = AmountText.ViewState.preview,
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
                    text = state.title ?: state.localizer.localize("APP.GENERAL.AMOUNT"),
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

            val focusManager = LocalFocusManager.current

            if (state.maxAction != null) {
                PlatformPillButton(
                    action = {
                        state.maxAction.invoke()
                        focusManager.clearFocus()
                    },
                    content = {
                        Text(
                            text = state.localizer.localize("APP.GENERAL.MAX"),
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_tertiary)
                                .themeFont(fontSize = ThemeFont.FontSize.mini),
                        )
                    },
                )
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
                text = state.footer ?: state.localizer.localize("APP.GENERAL.AVAILABLE"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
                maxLines = 1,
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
            PlatformAmountChange(
                modifier = Modifier.weight(1f),
                before = if (state.footerBefore != null) { {
                    AmountText.Content(
                        state = state.footerBefore,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontType = ThemeFont.FontType.number, fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                } } else {
                    null
                },
                after = if (state.footerAfter != null) { {
                    AmountText.Content(
                        state = state.footerAfter,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontType = ThemeFont.FontType.number, fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                } } else {
                    null
                },
                direction = PlatformDirection.from(state.footerBefore?.amount, state.footerAfter?.amount),
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }
}
