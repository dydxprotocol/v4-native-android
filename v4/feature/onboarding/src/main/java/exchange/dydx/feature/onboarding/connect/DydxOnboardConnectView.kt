package exchange.dydx.feature.onboarding.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
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
import exchange.dydx.trading.feature.shared.views.ProgressStepView

@Preview
@Composable
fun Preview_DydxOnboardConnectView() {
    DydxThemedPreviewSurface {
        DydxOnboardConnectView.Content(
            Modifier,
            DydxOnboardConnectView.ViewState.preview,
        )
    }
}

object DydxOnboardConnectView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val walletIcon: String? = null,
        var steps: List<ProgressStepView.ViewState> = listOf(),
        val closeButtonHandler: () -> Unit = {},
        val linkWalletAction: () -> Unit = {},
        val linkWalletButtonEnabled: Boolean = true,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                steps = listOf(
                    ProgressStepView.ViewState.preview,
                    ProgressStepView.ViewState.preview,
                    ProgressStepView.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxOnboardConnectViewModel = hiltViewModel<DydxOnboardConnectViewModel>()
        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier
                .background(ThemeColor.SemanticColor.layer_2.color)
                .fillMaxSize(),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.ONBOARDING.SIGN_MESSAGE"),
                icon = state.walletIcon ?: exchange.dydx.trading.feature.shared.R.drawable.icon_wc_logo,
                closeAction = { state.closeButtonHandler?.invoke() },
            )
            Text(
                text = state.localizer.localize("APP.ONBOARDING.TWO_SIGNATURE_REQUESTS"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
            ) {
                state.steps.forEach {
                    ProgressStepView(it).Content(Modifier)

                    Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                }
            }

            PlatformButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                text = state.localizer.localize("APP.ONBOARDING.LINK_WALLET"),
                state = if (state.linkWalletButtonEnabled) PlatformButtonState.Primary else PlatformButtonState.Disabled,
            ) {
                if (state.linkWalletButtonEnabled) {
                    state.linkWalletAction?.invoke()
                }
            }
        }
    }
}
