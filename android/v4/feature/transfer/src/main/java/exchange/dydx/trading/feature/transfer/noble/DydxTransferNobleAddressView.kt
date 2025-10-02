package exchange.dydx.trading.feature.transfer.noble

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
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
import exchange.dydx.trading.feature.shared.views.HeaderView
import exchange.dydx.utilities.utils.applyLink
import net.glxn.qrgen.android.QRCode

@Preview
@Composable
fun Preview_DydxTransferNobleAddressView() {
    DydxThemedPreviewSurface {
        DydxTransferNobleAddressView.Content(
            Modifier,
            DydxTransferNobleAddressView.ViewState.preview,
        )
    }
}

object DydxTransferNobleAddressView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val address: String?,
        val backButtonAction: (() -> Unit)? = null,
        val copyAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                address = "0x1234567890abcdef1234567890abcdef12345678",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferNobleAddressViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val listState = rememberLazyListState()

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

            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = 24.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
            ) {
                item {
                    Text(
                        text = createTitleString(state.localizer),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.medium),
                        modifier = Modifier,
                        textAlign = TextAlign.Center,
                    )
                }

                if (state.address != null) {
                    item {
                        QRCodeContent(
                            modifier = Modifier,
                            state = state,
                        )
                    }

                    item {
                        AddressContent(
                            modifier = Modifier,
                            state = state,
                        )
                    }
                }

                item {
                    WarningContent(
                        modifier = Modifier,
                        state = state,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PlatformButton(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(bottom = ThemeShapes.VerticalPadding * 2),
                text = state.localizer.localize("APP.ONBOARDING.COPY_NOBLE"),
            ) {
                state.copyAction?.invoke()
            }
        }
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
        val width = screenWidthPx * 2 / 3

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
                    .padding(16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
            )

            PlatformImage(
                icon = R.drawable.icon_noble,
                modifier = Modifier
                    .size(configuration.screenWidthDp.dp / 8),
            )
        }
    }

    @Composable
    private fun AddressContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            Text(
                text = state.localizer.localize("APP.ONBOARDING.YOUR_NOBLE_ADDRESS"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.medium)
                    .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                modifier = Modifier,
                textAlign = TextAlign.Center,
            )

            val shape = RoundedCornerShape(size = 16.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .themeColor(ThemeColor.SemanticColor.layer_3),
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = state.address ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.large)
                            .themeColor(foreground = ThemeColor.SemanticColor.text_primary),
                        modifier = Modifier
                            .weight(1f),
                        textAlign = TextAlign.Center,
                    )

                    PlatformImage(
                        icon = R.drawable.icon_copy,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                state.copyAction?.invoke()
                            },
                        colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_primary.color),
                    )
                }
            }
        }
    }

    @Composable
    private fun WarningContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlatformImage(
                icon = R.drawable.icon_warning,
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        state.copyAction?.invoke()
                    },
                colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.color_yellow.color),
            )

            Text(
                text = state.localizer.localize("WARNINGS.ONBOARDING.NOBLE_CHAIN_ONLY"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(foreground = ThemeColor.SemanticColor.color_yellow),
                modifier = Modifier
                    .weight(1f),
            )
        }
    }

    private fun createTitleString(
        localizer: LocalizerProtocol,
    ): AnnotatedString {
        return buildAnnotatedString {
            var newString = localizer.localize("APP.DEPOSIT_MODAL.TO_DEPOSIT_FROM_CEX")
            addStyle(
                style = SpanStyle(
                    color = ThemeColor.SemanticColor.text_tertiary.color,
                ),
                start = 0,
                end = newString.length,
            )
            newString = applyLink(
                value = localizer.localize("APP.DEPOSIT_MODAL.TO_DEPOSIT_FROM_CEX"),
                key = "{ASSET}",
                replacement = "USDC",
                link = null,
                linkColor = ThemeColor.SemanticColor.text_primary.color,
            )
            newString = applyLink(
                value = newString,
                key = "{NETWORK}",
                replacement = "Noble Network",
                link = null,
                linkColor = ThemeColor.SemanticColor.text_primary.color,
            )
            append(newString)
        }
    }
}
