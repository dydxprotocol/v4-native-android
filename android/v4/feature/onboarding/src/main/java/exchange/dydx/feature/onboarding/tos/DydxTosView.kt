package exchange.dydx.feature.onboarding.tos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.dividers.PlatformDivider
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
import exchange.dydx.trading.feature.shared.views.HeaderView
import exchange.dydx.utilities.utils.applyLink

@Preview
@Composable
fun Preview_DydxTosView() {
    DydxThemedPreviewSurface {
        DydxTosView.Content(Modifier, DydxTosView.ViewState.preview)
    }
}

object DydxTosView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val tosUrl: String?,
        val privacyPolicyUrl: String?,
        val closeAction: (() -> Unit)? = null,
        val ctaAction: (() -> Unit)? = null,
        val urlAction: ((String) -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                tosUrl = "https://dydx.exchange",
                privacyPolicyUrl = "https://dydx.exchange",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTosViewModel = hiltViewModel()

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
                .themeColor(background = ThemeColor.SemanticColor.layer_2)
                .fillMaxSize()
                .padding(
                    vertical = ThemeShapes.VerticalPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.ONBOARDING.ACKNOWLEDGE_TERMS"),
                closeAction = state.closeAction,
            )

            PlatformDivider()

            TosContent(modifier = Modifier.weight(1f), state = state)

            PlatformButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                text = state.localizer.localize("APP.ONBOARDING.I_AGREE"),
                state = PlatformButtonState.Primary,
                action = state.ctaAction ?: {},
            )
        }
    }

    @Composable
    private fun TosContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                )
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CreateHeader(modifier = Modifier, state = state)
            CreateLines(modifier = Modifier, state = state)
            CreateFooter(modifier = Modifier, state = state)
        }
    }

    @Composable
    private fun CreateHeader(
        modifier: Modifier,
        state: ViewState,
    ) {
        val agreementText = state.localizer.localize("APP.ONBOARDING.TOS_TITLE")

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

    @Composable
    private fun CreateLines(
        modifier: Modifier,
        state: ViewState,
    ) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(ThemeColor.SemanticColor.layer_3.color, shape)
                .clip(shape)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            listOf(
                "APP.ONBOARDING.TOS_LINE1",
                "APP.ONBOARDING.TOS_LINE2",
                "APP.ONBOARDING.TOS_LINE3",
                "APP.ONBOARDING.TOS_LINE4",
                "APP.ONBOARDING.TOS_LINE5",
            ).forEach {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "•",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                    Text(
                        text = state.localizer.localize(it),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }
            }
        }
    }

    @Composable
    private fun CreateFooter(
        modifier: Modifier,
        state: ViewState,
    ) {
        Text(
            modifier = modifier,
            text = state.localizer.localize("APP.ONBOARDING.TOS_TRANSLATION_DISCLAIMER"),
            style = TextStyle.dydxDefault
                .themeFont(fontSize = ThemeFont.FontSize.small)
                .themeColor(ThemeColor.SemanticColor.text_tertiary),
        )
    }
}
