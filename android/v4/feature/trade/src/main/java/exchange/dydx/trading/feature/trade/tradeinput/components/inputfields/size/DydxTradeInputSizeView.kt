package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.size

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformAccessoryButton
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformImage
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
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold
import exchange.dydx.trading.feature.shared.views.LabeledTextInput
import exchange.dydx.trading.feature.shared.views.TokenTextView

@Preview
@Composable
fun Preview_DydxTradeInputSizeView() {
    DydxThemedPreviewSurface {
        DydxTradeInputSizeView.Content(Modifier, DydxTradeInputSizeView.ViewState.preview)
    }
}

object DydxTradeInputSizeView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val token: String? = null,
        val size: String? = null,
        val usdcSize: String? = null,
        val placeholder: String? = null,
        val onSizeChanged: (String) -> Unit = {},
        val onUsdcSizeChanged: (String) -> Unit = {},
        val showingUsdc: Boolean = false,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                token = "WETH",
                size = "1.0",
                usdcSize = "1.0",
                placeholder = "0.0",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputSizeViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val showingUsdc = remember { mutableStateOf(state.showingUsdc) }
        val focusManager = LocalFocusManager.current

        InputFieldScaffold(modifier) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabeledTextInput.Content(
                        modifier = Modifier.weight(1f),
                        state = LabeledTextInput.ViewState(
                            localizer = state.localizer,
                            label = state?.localizer?.localize("APP.GENERAL.AMOUNT"),
                            token = if (showingUsdc.value) "USD" else state.token,
                            value = if (showingUsdc.value) state.usdcSize else state.size,
                            placeholder = if (showingUsdc.value) {
                                "0.00"
                            } else {
                                state.placeholder
                                    ?: ""
                            },
                            onValueChanged = {
                                if (showingUsdc.value) {
                                    state.onUsdcSizeChanged(it)
                                } else {
                                    state.onSizeChanged(it)
                                }
                            },
                        ),
                    )

                    PlatformAccessoryButton(
                        modifier = Modifier,
                        action = {
                            focusManager.clearFocus()
                            showingUsdc.value = !showingUsdc.value
                        },
                    ) {
                        Column(
                            modifier = Modifier.padding(ThemeShapes.InputPaddingValues),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            PlatformImage(
                                modifier = Modifier.size(18.dp),
                                icon = R.drawable.icon_switch,
                                colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_secondary.color),
                            )
                            Text(
                                text = if (showingUsdc.value) state.token ?: "" else "USD",
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                                    .themeColor(ThemeColor.SemanticColor.text_secondary),
                            )
                        }
                    }
                }

                val value = if (showingUsdc.value) state.size else state.usdcSize
                PlatformDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = value ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                        modifier = Modifier.padding(ThemeShapes.InputPaddingValues),
                    )
                    if (!value.isNullOrEmpty()) {
                        TokenTextView.Content(
                            modifier = Modifier,
                            state = TokenTextView.ViewState(
                                symbol = if (!showingUsdc.value) "USD" else state.token ?: "",
                            ),
                            textStyle = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.tiny)
                                .themeColor(ThemeColor.SemanticColor.text_tertiary),
                        )
                    }
                }
            }
        }
    }
}
