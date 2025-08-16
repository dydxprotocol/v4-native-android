package exchange.dydx.feature.onboarding.desktopscan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.integration.javascript.JavascriptRunnerWebview
import exchange.dydx.platformui.components.PlatformDialogScaffold
import exchange.dydx.platformui.components.camera.PlatformQrScanner
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderView
import exchange.dydx.trading.integration.analytics.logging.ConsoleLogger
import exchange.dydx.utilities.utils.Logging

@Preview
@Composable
fun Preview_dydxDesktopScanView() {
    DydxThemedPreviewSurface {
        DydxDesktopScanView.Content(Modifier, DydxDesktopScanView.ViewState.preview)
    }
}

object DydxDesktopScanView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val logger: Logging,
        val backButtonHandler: (() -> Unit)? = null,
        val closeButtonHandler: () -> Unit = {},
        val qrCodeScannedHandler: (String) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                logger = ConsoleLogger(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxDesktopScanViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)

        PlatformDialogScaffold(dialog = viewModel.platformDialog)

        JavascriptRunnerWebview(
            modifier = Modifier,
            isVisible = false,
            javascriptRunner = viewModel.starkexLib.runner,
            logger = state?.logger,
        )
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
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                ),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.ONBOARDING.SCAN_QR_CODE"),
                backAction = state.backButtonHandler,
                closeAction = { state.closeButtonHandler.invoke() },
            )

            PlatformDivider()

            PlatformQrScanner(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                promptText = state.localizer.localize("APP.CAMERA.ENABLE_CAMERA_TO_SCAN"),
                buttonText = state.localizer.localize("APP.CAMERA.ALLOW_ACCESS"),
                callback = {
                    state.qrCodeScannedHandler.invoke(it)
                },
            )
        }
    }
}
