package exchange.dydx.feature.onboarding.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.icons.PlatformRoundIcon
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.isLightTheme
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton
import exchange.dydx.utilities.utils.applyLink

@Preview
@Composable
fun Preview_DydxOnboardWelcomeView() {
    DydxThemedPreviewSurface {
        DydxOnboardWelcomeView.Content(Modifier, DydxOnboardWelcomeView.ViewState.preview)
    }
}

object DydxOnboardWelcomeView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val ctaAction: (() -> Unit)?,
        val closeAction: (() -> Unit)?,
        val tosUrl: String?,
        val privacyPolicyUrl: String?,
        val urlAction: ((String) -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                ctaAction = null,
                closeAction = null,
                tosUrl = null,
                privacyPolicyUrl = null,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxOnboardWelcomeViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .background(ThemeColor.SemanticColor.layer_2.color)
                .fillMaxSize(),
        ) {
            HeaderView(
                modifier = Modifier,
                state = state,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .weight(1f)
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    )
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
            ) {
                Text(
                    text = state.localizer.localize("APP.ONBOARDING.WELCOME_TEXT"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )

                Spacer(modifier = Modifier.size(ThemeShapes.VerticalPadding))

                ItemView(
                    modifier = Modifier,
                    icon = R.drawable.onboard_powerful,
                    titleKey = "APP.ONBOARDING.VALUE_PROP_ADVANCED",
                    subtitleKey = "APP.ONBOARDING.VALUE_PROP_ADVANCED_DESC",
                    localizer = state.localizer,
                )
                ItemView(
                    modifier = Modifier,
                    icon = R.drawable.onboard_advanced,
                    titleKey = "APP.ONBOARDING.VALUE_PROP_LIQUID",
                    subtitleKey = "APP.ONBOARDING.VALUE_PROP_LIQUID_DESC",
                    localizer = state.localizer,
                )
                ItemView(
                    modifier = Modifier,
                    icon = R.drawable.onboard_trustless,
                    titleKey = "APP.ONBOARDING.VALUE_PROP_TRUSTLESS",
                    subtitleKey = "APP.ONBOARDING.VALUE_PROP_TRUSTLESS_DESC",
                    localizer = state.localizer,
                )

                Spacer(modifier = Modifier.size(ThemeShapes.VerticalPadding))

                FooterFiew(
                    modifier = Modifier,
                    state = state,
                )
            }

            PlatformButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                text = state.localizer.localize("APP.ONBOARDING.GET_STARTED"),
            ) {
                state.ctaAction?.invoke()
            }
        }
    }

    @Composable
    private fun HeaderView(
        modifier: Modifier = Modifier,
        state: ViewState,
    ) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(
                    vertical = ThemeShapes.VerticalPadding,
                )
                .padding(top = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))

            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
            ) {
                PlatformImage(
                    icon = if (ThemeSettings.shared.isLightTheme()) R.drawable.dydx_logo_light else R.drawable.dydx_logo_dark,
                    modifier = Modifier
                        .height(41.dp)
                        .background(
                            color = exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_2.color,
                        ),
                )
            }

            HeaderViewCloseBotton(closeAction = state.closeAction)
        }
    }

    @Composable
    private fun ItemView(
        modifier: Modifier = Modifier,
        icon: Any,
        titleKey: String,
        subtitleKey: String,
        localizer: LocalizerProtocol,
    ) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(
                    vertical = ThemeShapes.VerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            PlatformRoundIcon(
                icon = icon,
            )

            Column(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = localizer.localize(titleKey),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                    modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                )

                Text(
                    text = localizer.localize(subtitleKey),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                )
            }
        }
    }

    @Composable
    private fun FooterFiew(
        modifier: Modifier,
        state: ViewState,
    ) {
        val agreementText = state.localizer.localize("APP.ONBOARDING.YOU_AGREE_TO_TERMS")

        val value = buildAnnotatedString {
            var newString = applyLink(
                value = agreementText,
                key = "{TERMS_LINK}",
                replacement = state.localizer.localize("APP.HEADER.TERMS_OF_USE"),
                link = state.tosUrl,
                linkColor = ThemeColor.SemanticColor.color_purple.color,
            )
            newString = applyLink(
                value = newString,
                key = "{PRIVACY_POLICY_LINK}",
                replacement = state.localizer.localize("APP.ONBOARDING.PRIVACY_POLICY"),
                link = state.privacyPolicyUrl,
                linkColor = ThemeColor.SemanticColor.color_purple.color,
            )

            append(newString)
        }

        ClickableText(
            modifier = modifier,
            text = value,
            style = TextStyle.dydxDefault
                .themeFont(fontSize = ThemeFont.FontSize.small)
                .themeColor(ThemeColor.SemanticColor.text_tertiary),
            onClick = {
                value.getStringAnnotations(start = it, end = it)
                    .firstOrNull()
                    ?.let { annotation ->
                        state.urlAction?.invoke(annotation.item)
                    }
            },
        )
    }
}
