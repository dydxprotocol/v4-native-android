package exchange.dydx.feature.onboarding.debugscan

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderView
import net.glxn.qrgen.android.QRCode

@Preview
@Composable
fun Preview_DydxDebugScanView() {
    DydxThemedPreviewSurface {
        DydxDebugScanView.Content(Modifier, DydxDebugScanView.ViewState.preview)
    }
}

object DydxDebugScanView : DydxComponent {

    data class ViewState(
        var uri: String? = null,
        var error: String? = null,
        var closeButtonHandler: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                uri = "https://dydx.exchange",
                error = null,
                closeButtonHandler = {},
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxDebugScanViewModel = hiltViewModel()
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            viewModel.updateContext(context)
        }

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        if (state != null) {
            Content(modifier, state)
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState) {
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
                title = "Debug Scan",
                backAction = {
                    state.closeButtonHandler.invoke()
                },
            )

            PlatformDivider()

            val uri = state.uri
            if (uri != null) {
                val qr = QRCode.from(state.uri)
                    .withSize(320, 320)
                    .bitmap()

                Image(
                    bitmap = qr.asImageBitmap(),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f),
                )

                Text(
                    text = uri,
                    style = androidx.compose.ui.text.TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            } else {
                state?.error?.let {
                    Text(
                        text = it,
                        style = androidx.compose.ui.text.TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(ThemeShapes.HorizontalPadding))
        }
    }
}
