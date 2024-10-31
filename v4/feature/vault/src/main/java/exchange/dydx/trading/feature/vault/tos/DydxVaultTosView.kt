package exchange.dydx.trading.feature.vault.tos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton

@Preview
@Composable
fun Preview_DydxVaultTosView() {
    DydxThemedPreviewSurface {
        DydxVaultTosView.Content(Modifier, DydxVaultTosView.ViewState.preview)
    }
}

object DydxVaultTosView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val operatorDesc: String? = null,
        val operatorLearnMore: String? = null,
        val vaultDesc: String? = null,
        val operatorAction: (() -> Unit)? = null,
        val vaultAction: (() -> Unit)? = null,
        val ctaButtonAction: (() -> Unit)? = null,
        val dydxChainLogoUrl: String? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                operatorDesc = "Operator description",
                vaultDesc = "Vault description",
                operatorLearnMore = "Learn more about operator",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultTosViewModel = hiltViewModel()

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
                .themeColor(ThemeColor.SemanticColor.layer_3),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            HeaderContent(Modifier, state)

            PlatformDivider()

            Column(
                modifier = Modifier
                    .padding(ThemeShapes.HorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
            ) {
                Text(
                    text = state.vaultDesc ?: "",
                    modifier = Modifier,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )

                ClickableText(
                    modifier = Modifier,
                    text = buildAnnotatedString {
                        append(state.localizer.localize("APP.VAULTS.LEARN_MORE_ABOUT_MEGAVAULT"))
                    },
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.color_purple),
                    onClick = {
                        state.vaultAction?.invoke()
                    },
                )
            }

            Column(
                modifier = Modifier
                    .padding(ThemeShapes.HorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
            ) {
                Text(
                    text = state.operatorDesc ?: "",
                    modifier = Modifier,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )

                ClickableText(
                    modifier = Modifier,
                    text = buildAnnotatedString {
                        append(state.operatorLearnMore ?: "")
                    },
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.color_purple),
                    onClick = {
                        state.operatorAction?.invoke()
                    },
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PlatformButton(
                modifier = modifier.fillMaxWidth().padding(ThemeShapes.HorizontalPadding),
                text = state.localizer.localize("APP.GENERAL.OK"),
                state = PlatformButtonState.Primary,
            ) {
                state.ctaButtonAction?.invoke()
            }
        }
    }

    @Composable
    private fun HeaderContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(vertical = ThemeShapes.VerticalPadding)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            PlatformImage(
                icon = state.dydxChainLogoUrl,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
            )

            Text(
                text = state.localizer.localize("APP.VAULTS.MEGAVAULT"),
                modifier = Modifier,
                style = TextStyle.dydxDefault
                    .themeFont(
                        fontType = ThemeFont.FontType.plus,
                        fontSize = ThemeFont.FontSize.extra,
                    )
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )

            Spacer(modifier = Modifier.weight(1f))

            HeaderViewCloseBotton(closeAction = state.ctaButtonAction)
        }
    }
}
