package exchange.dydx.trading.feature.transfer.fiat

import android.R.attr.text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.localizeWithParams
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.inputs.PlatformTextInput
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_dydxFiatDepositView() {
    DydxThemedPreviewSurface {
        DydxFiatDepositView.Content(Modifier, DydxFiatDepositView.ViewState.preview)
    }
}

object DydxFiatDepositView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val backButtonAction: (() -> Unit)? = null,
        val ctaAction: (() -> Unit)? = null,
        val providerName: String? = null,
        val providerSubtitle: String? = null,
        val fee: String? = null,
        val amountSubtitle: String? = null,
        val providerIcon: Any? = null,
        val ctaEnabled: Boolean = false,

        val value: String ? = null,
        val onEditAction: ((String) -> Unit)? = null,
    ) {
        companion object Companion {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                providerName = "MoonPay",
                providerSubtitle = "Fast, secure fiat onramp",
                fee = "$1.00",
                amountSubtitle = "min $101.00",
                ctaEnabled = true,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxFiatDepositViewModel = hiltViewModel()

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
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.GENERAL.DEPOSIT"),
                backAction = state.backButtonAction,
            )

            PlatformDivider()

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding * 2),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier,
                    text = "$",
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontType = ThemeFont.FontType.plus,
                            rawSize = 48.0,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                PlatformTextInput(
                    modifier = Modifier.wrapContentWidth(),
                    value = state.value ?: "",
                    textStyle = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(
                            fontType = ThemeFont.FontType.plus,
                            rawSize = 48.0,
                        ).copy(
                            textAlign = TextAlign.Center, // centers cursor within text
                        ),
                    placeHolder = if (state.value.isNullOrEmpty()) {
                        state.formatter.raw(0.0, 2)
                    } else {
                        null
                    },
                    onValueChange = { state.onEditAction?.invoke(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    centeredText = true,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ProviderInfoContent(
                modifier = Modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(bottom = ThemeShapes.VerticalPadding * 2),
                state = state,
            )

            PlatformButton(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(bottom = ThemeShapes.VerticalPadding * 2),
                state = if (state.ctaEnabled) {
                    PlatformButtonState.Primary
                } else {
                    PlatformButtonState.Disabled
                },
                text = state.localizer.localizeWithParams(
                    path = "APP.DEPOSIT_WITH_FIAT.CONTINUE_TO",
                    params = mapOf("PROVIDER" to (state.providerName ?: "Provider")),
                ),
            ) {
                state.ctaAction?.invoke()
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(bottom = 16.dp),
                text = state.localizer.localizeWithParams(
                    path = "APP.DEPOSIT_WITH_FIAT.CONTINUE_TO_DISCLAIMER",
                    params = mapOf(
                        "PROVIDER" to (state.providerName ?: "Provider"),
                    ),
                ),
                textAlign = TextAlign.Center,
                style = TextStyle.dydxDefault
                    .themeFont(
                        fontSize = ThemeFont.FontSize.tiny,
                    )
                    .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }

    @Composable
    private fun ProviderInfoContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlatformImage(
                icon = state.providerIcon, // R.drawable.icon_moonpay,
                modifier = Modifier
                    .size(24.dp),
            )

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = state.providerName ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontType = ThemeFont.FontType.plus,
                            fontSize = ThemeFont.FontSize.base,
                        )
                        .themeColor(foreground = ThemeColor.SemanticColor.text_primary),
                )

                Text(
                    text = state.providerSubtitle ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.base,
                        )
                        .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = state.fee ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.base,
                        )
                        .themeColor(foreground = ThemeColor.SemanticColor.text_primary),
                )

                Text(
                    text = state.amountSubtitle ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.base,
                        )
                        .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                )
            }
        }
    }
}
