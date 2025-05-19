package exchange.dydx.trading.core.biometric

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_DydxBiometricView() {
    DydxThemedPreviewSurface {
        DydxBiometricView.Content(Modifier, DydxBiometricView.ViewState.preview)
    }
}

object DydxBiometricView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val text: String? = null,
        val retryAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                text = "Biometric authentication failed",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, error: String?, retryAction: () -> Unit) {
        val viewModel: DydxBiometricViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value ?: return
        Content(
            modifier,
            state.copy(text = error, retryAction = retryAction),
        )
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = ThemeShapes.VerticalPadding)
                .themeColor(ThemeColor.SemanticColor.layer_2),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = state.text ?: "",
                style = TextStyle.dydxDefault,
                textAlign = TextAlign.Center,
            )

            PlatformButton(
                modifier = Modifier.fillMaxWidth(),
                text = state.localizer.localize("APP.GENERAL.AUTHENTICATE_WITH_BIOMETRICS"),
                action = state.retryAction,
            )
        }
    }
}
