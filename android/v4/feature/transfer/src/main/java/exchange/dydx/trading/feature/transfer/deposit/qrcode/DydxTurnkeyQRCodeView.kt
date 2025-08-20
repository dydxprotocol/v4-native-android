package exchange.dydx.trading.feature.transfer.deposit.qrcode

import android.R.attr.bottom
import android.R.attr.shape
import android.R.attr.text
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.icons.PlatformRoundImage
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
import exchange.dydx.trading.feature.receipt.validation.DydxValidationView
import exchange.dydx.trading.feature.receipt.validation.DydxValidationView.State
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.HeaderView
import net.glxn.qrgen.android.QRCode

@Preview
@Composable
fun Preview_DydxTurnkeyQRCodeView() {
    DydxThemedPreviewSurface {
        DydxTurnkeyQRCodeView.Content(Modifier, DydxTurnkeyQRCodeView.ViewState.preview)
    }
}

object DydxTurnkeyQRCodeView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val backAction: (() -> Unit)? = null,
        val subtitle: String? = null,
        val footer: String? = null,
        val address: String? = null,
        val chainIconUrl: String? = null,
        val onCopyAction: (() -> Unit)? = null,
        val copied: Boolean = true
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                subtitle = "Scan the QR code to deposit USDC",
                footer = "Powered by dYdX Turnkey",
                address = "0x1234567890abcdef1234567890abcdef12345678",
                chainIconUrl = "https://example.com/icon.png",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTurnkeyQRCodeViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier.fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            HeaderView(
                title = "",
                backAction = { state.backAction?.invoke() },
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.DEPOSIT"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.large)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
                Text(
                    text = state.subtitle ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!state.address.isNullOrEmpty()) {
                    val shape = RoundedCornerShape(16.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(shape)
                            .border(
                                width = 2.dp,
                                shape = shape,
                                color = ThemeColor.SemanticColor.layer_3.color,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                        ) {
                            PlatformRoundImage(
                                icon = state.chainIconUrl,
                                size = 36.dp,
                                modifier = Modifier.padding(24.dp),
                            )
                        }

                        Box(
                            modifier = Modifier.weight(1f),
                        ) {
                            QRCodeContent(
                                modifier = Modifier
                                    .height(LocalConfiguration.current.screenWidthDp.dp / 2)
                                    .align(Alignment.Center),
                                state = state,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        AddressText(
                            modifier = Modifier.weight(1f),
                            state = state,
                        )

                        if (state.copied) {
                            CopiedButton(
                                modifier = Modifier.align(Alignment.CenterVertically).weight(1f),
                                state = state,
                            )
                        } else {
                            CopyButton(
                                modifier = Modifier.align(Alignment.CenterVertically).weight(1f),
                                state = state,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                DydxValidationView.Content(
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = ThemeShapes.VerticalPadding * 2),
                    state = DydxValidationView.ViewState(
                        localizer = state.localizer,
                        state = State.Warning,
                        message = state.footer,
                    ),
                )
            }
        }
    }

    @Composable
    fun AddressText(
        modifier: Modifier,
        state: ViewState,
    ) {
        val address = state.address ?: return
        val highlightColor = ThemeColor.SemanticColor.text_primary.color

        val styledText: AnnotatedString = buildAnnotatedString {
            val length = address.length

            if (length <= 12) {
                // If the text is too short, just highlight all of it
                withStyle(SpanStyle(color = highlightColor)) {
                    append(address)
                }
            } else {
                // First 6
                withStyle(SpanStyle(color = highlightColor)) {
                    append(address.take(6))
                }

                // Middle (default color)
                append(address.drop(6).dropLast(6))

                // Last 6
                withStyle(SpanStyle(color = highlightColor)) {
                    append(address.takeLast(6))
                }
            }
        }

        Text(
            text = styledText,
            modifier = modifier,
            style = TextStyle.dydxDefault
                .themeFont(fontSize = ThemeFont.FontSize.base)
                .themeColor(ThemeColor.SemanticColor.text_tertiary),
        )
    }

    @Composable
    private fun CopyButton(
        modifier: Modifier,
        state: ViewState,
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current

        val shape = RoundedCornerShape(16.dp)
        PlatformButton(
            text = state.localizer.localize("APP.GENERAL.COPY"),
            leadingContent = {
                PlatformImage(
                    icon = R.drawable.icon_copy,
                    modifier = Modifier.size(16.dp),
                    colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.color_white.color),
                )
            },
            state = PlatformButtonState.Primary,
            cornerRadius = 16.dp,
            modifier = modifier
                .clip(shape),
            action = {
                clipboardManager.setText(AnnotatedString(state.address ?: ""))
                state.onCopyAction?.invoke()
            },
        )
    }

    @Composable
    private fun CopiedButton(
        modifier: Modifier,
        state: ViewState,
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current

        val shape = RoundedCornerShape(16.dp)
        PlatformButton(
            text = state.localizer.localize("APP.GENERAL.COPIED"),
            leadingContent = {
                PlatformImage(
                    icon = R.drawable.icon_check,
                    modifier = Modifier.size(16.dp),
                    colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.color_green.color),
                )
            },
            state = PlatformButtonState.Custom(
                borderColor = ThemeColor.SemanticColor.transparent,
                textColor = ThemeColor.SemanticColor.color_green,
                enabled = true,
                backgroundColor = ThemeColor.SemanticColor.color_faded_green,
            ),
            cornerRadius = 16.dp,
            modifier = modifier
                .clip(shape),
            action = {
                clipboardManager.setText(AnnotatedString(state.address ?: ""))
                state.onCopyAction?.invoke()
            },
        )
    }

    @Composable
    private fun QRCodeContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        val foregroundColor = ThemeColor.SemanticColor.text_primary.color
        val backgroundColor = ThemeColor.SemanticColor.transparent.color

        val configuration = LocalConfiguration.current
        val screenWidthPx = configuration.screenWidthDp * configuration.densityDpi / 160
        val width = screenWidthPx

        val qr = QRCode.from(state.address)
            .withSize(width, width)
            .withColor(foregroundColor.toArgb(), backgroundColor.toArgb())
            .bitmap()

        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = qr.asImageBitmap(),
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                contentDescription = "QR Code",
                modifier = Modifier
                    .fillMaxWidth(),
            )

            PlatformImage(
                icon = exchange.dydx.trading.common.R.drawable.logo_no_fill,
                modifier = Modifier
                    .size(configuration.screenWidthDp.dp / 10),
            )
        }
    }
}
