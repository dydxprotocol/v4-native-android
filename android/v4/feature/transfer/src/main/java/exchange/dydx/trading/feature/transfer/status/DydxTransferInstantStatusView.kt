package exchange.dydx.trading.feature.transfer.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.progress.PlatformIndeterminateProgress
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

@Preview
@Composable
fun Preview_dydxTransferInstantStatusView() {
    DydxThemedPreviewSurface {
        DydxTransferInstantStatusView.Content(
            Modifier,
            DydxTransferInstantStatusView.ViewState.preview,
        )
    }
}

object DydxTransferInstantStatusView : DydxComponent {
    enum class StatusIcon {
        SUBMITTING, FAILED, SUCCESS;
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val title: String? = null,
        val subtitle: String? = null,
        val label: String? = null,
        val amount: String? = null,
        val token: String? = null,
        val chainIconUri: String? = null,
        val tokenIconUri: String? = null,
        val status: StatusIcon = StatusIcon.SUBMITTING,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                title = "Deposit in progress",
                subtitle = "Your deposit is currently being processed.",
                label = "Your deposit",
                amount = "100.00",
                token = "DAI",
                chainIconUri = "https://v4.testnet.dydx.exchange/chains/ethereum.png",
                tokenIconUri = "https://v4.testnet.dydx.exchange/currencies/usdc.png",
                status = StatusIcon.SUBMITTING,
            )
        }

        val statusTitle: String
            get() = when (status) {
                StatusIcon.SUBMITTING -> localizer.localize("APP.TRADE.SUBMITTING")
                StatusIcon.FAILED -> localizer.localize("APP.TRADE.FAILED")
                StatusIcon.SUCCESS -> localizer.localize("APP.TRADE.SUCCESS")
            }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferInstantStatusViewModel = hiltViewModel()

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                ProgressContent(
                    modifier = Modifier,
                    state = state,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                BottomItemsContent(
                    modifier = Modifier,
                    state = state,
                )

                PlatformButton(
                    text = state.localizer.localize("APP.GENERAL.DONE"),
                    state = PlatformButtonState.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    state.closeAction?.invoke()
                }
            }
        }
    }

    @Composable
    private fun ProgressContent(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state.status) {
                StatusIcon.SUBMITTING -> {
                    Box(
                        modifier = Modifier
                            .size(72.dp),
                    ) {
                        PlatformIndeterminateProgress(
                            modifier = Modifier,
                            size = 72.dp,
                            outerTrackColor = ThemeColor.SemanticColor.color_purple,
                            trackColor = ThemeColor.SemanticColor.text_tertiary,
                        )
                    }
                }

                StatusIcon.FAILED -> {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(ThemeColor.SemanticColor.color_faded_red.color, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                    ) {
                        PlatformImage(
                            modifier = Modifier
                                .size(43.dp),
                            icon = R.drawable.close,
                            colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.color_red.color),
                        )
                    }
                }

                StatusIcon.SUCCESS -> {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(ThemeColor.SemanticColor.color_faded_green.color, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                    ) {
                        PlatformImage(
                            modifier = Modifier
                                .size(43.dp),
                            icon = R.drawable.icon_check,
                            colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.color_green.color),
                        )
                    }
                }
            }

            Text(
                text = state.title ?: "",
                textAlign = TextAlign.Center,
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_primary)
                    .themeFont(fontSize = ThemeFont.FontSize.large, fontType = ThemeFont.FontType.plus),
                modifier = Modifier
                    .padding(top = 24.dp)
                    .padding(horizontal = ThemeShapes.HorizontalPadding),
            )

            Text(
                text = state.subtitle ?: "",
                textAlign = TextAlign.Center,
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = ThemeShapes.HorizontalPadding),
            )
        }
    }

    @Composable
    private fun BottomItemsContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.label ?: "",
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                modifier = Modifier
                    .weight(1f),
            )

            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier.size(28.dp),
                ) {
                    PlatformImage(
                        icon = state.tokenIconUri,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                    )

                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                color = ThemeColor.SemanticColor.layer_5.color,
                                shape = CircleShape,
                            )
                            .align(Alignment.BottomEnd),
                    ) {
                        PlatformImage(
                            icon = state.chainIconUri,
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .align(Alignment.Center),
                        )
                    }
                }

                Text(
                    text = state.amount ?: "",
                    style = TextStyle.dydxDefault,
                    modifier = Modifier,
                )

                Text(
                    text = state.token ?: "",
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    modifier = Modifier,
                )
            }
        }
    }
}
