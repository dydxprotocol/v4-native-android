package exchange.dydx.trading.feature.transfer.components

import android.R.attr.text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.icons.PlatformImage
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
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold

@Preview
@Composable
fun Preview_InstantInputBox() {
    DydxThemedPreviewSurface {
        InstantInputBox.Content(Modifier, InstantInputBox.ViewState.preview)
    }
}

object InstantInputBox {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val value: String?,
        val valuePlaceholder: String?,
        val token: String,
        val maxAmount: Double?,
        val maxAmountString: String?,
        val tokenIconUri: String?,
        val chainIconUri: String?,
        val assetAction: () -> Unit = {},
        val maxAction: () -> Unit = {},
        val editAction: (String) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                value = null,
                valuePlaceholder = "0.00",
                token = "USDC",
                maxAmount = 1000.0,
                maxAmountString = "1000.00",
                tokenIconUri = "https://v4.testnet.dydx.exchange/currencies/usdc.png",
                chainIconUri = "https://v4.testnet.dydx.exchange/chains/ethereum.png",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val focusManager = LocalFocusManager.current

        InputFieldScaffold(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = state.localizer.localize("APP.GENERAL.AMOUNT"),
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_tertiary)
                                .themeFont(fontSize = ThemeFont.FontSize.mini),
                        )

                        Text(
                            text = state.maxAmountString ?: "",
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_tertiary)
                                .themeFont(fontSize = ThemeFont.FontSize.mini),
                        )

                        Text(
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    focusManager.clearFocus()
                                    state.maxAction()
                                },
                            text = state.localizer.localize("APP.GENERAL.MAX"),
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.color_purple)
                                .themeFont(fontSize = ThemeFont.FontSize.mini),
                        )
                    }

                    PlatformTextInput(
                        modifier = Modifier,
                        value = state.value ?: "",
                        textStyle = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_primary)
                            .themeFont(fontSize = ThemeFont.FontSize.large),
                        placeHolder = state.valuePlaceholder ?: "",
                        onValueChange = { state.editAction(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }

                TokenSelectorContent(
                    modifier = Modifier
                        .padding(vertical = ThemeShapes.VerticalPadding),
                    state = state,
                )
            }
        }
    }

    @Composable
    fun TokenSelectorContent(modifier: Modifier, state: ViewState) {
        PlatformButton(
            modifier = modifier,
            action = {
                state.assetAction()
            },
            contentPadding = PaddingValues(
                horizontal = 8.dp,
                vertical = ThemeShapes.VerticalPadding,
            ),
        ) {
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(28.dp),
                ) {
                    PlatformImage(
                        icon = state.tokenIconUri,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                    )

                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                color = ThemeColor.SemanticColor.layer_5.color,
                                shape = CircleShape,
                            )
                            .align(Alignment.BottomEnd),
                    ) {
                        PlatformImage(
                            icon = state.chainIconUri,
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .align(Alignment.Center),
                        )
                    }
                }

                Text(
                    text = state.token,
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                )

                Icon(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = "",
                    modifier = Modifier.size(10.dp),
                    tint = ThemeColor.SemanticColor.text_secondary.color,
                )
            }
        }
    }
}
