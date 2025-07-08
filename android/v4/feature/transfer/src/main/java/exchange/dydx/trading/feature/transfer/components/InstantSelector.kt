package exchange.dydx.trading.feature.transfer.components

import android.R.attr.shape
import android.R.attr.text
import android.R.attr.textColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.receipt.streams.TransferRouteSelection
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_InstantSelector() {
    DydxThemedPreviewSurface {
        InstantSelector.Content(Modifier, InstantSelector.ViewState.preview)
    }
}

object InstantSelector {
    enum class DepositSelectorViewStyle {
        TOGGLE,
        DISPLAY_ONLY
    }
    data class ViewState(
        val localizer: LocalizerProtocol,
        val uiStyle: DepositSelectorViewStyle = DepositSelectorViewStyle.DISPLAY_ONLY,
        val selection: TransferRouteSelection,
        val instantFee: String?,
        val regularTime: String?,
        val regularFee: String?,
        val selectionAction: (TransferRouteSelection) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                selection = TransferRouteSelection.Instant,
                instantFee = "$0.50",
                regularTime = "3-5 days",
                regularFee = "$1.00",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        when (state.uiStyle) {
            DepositSelectorViewStyle.TOGGLE ->
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    InstantSelectViewContent(modifier = Modifier.weight(1f), state = state)
                    RegularSelectViewContent(modifier = Modifier.weight(1f), state = state)
                }
            DepositSelectorViewStyle.DISPLAY_ONLY ->
                InstantSelectDisplayViewContent(
                    modifier = modifier.fillMaxWidth(),
                    state = state,
                )
        }
    }

    @Composable
    private fun InstantSelectDisplayViewContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = state.localizer.localize("APP.DEPOSIT_MODAL.DEPOSIT_METHOD"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            Spacer(modifier = Modifier.weight(1f))

            when (state.selection) {
                TransferRouteSelection.Instant ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        PlatformImage(
                            icon = R.drawable.icon_instant_deposit,
                            modifier = Modifier
                                .size(14.dp),
                        )

                        Text(
                            text = state.localizer.localize("APP.GENERAL.INSTANT"),
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_primary)
                                .themeFont(fontSize = ThemeFont.FontSize.small),
                        )

                        Text(
                            modifier = Modifier
                                .background(
                                    color = ThemeColor.SemanticColor.color_faded_purple.color,
                                    shape = RoundedCornerShape(size = 6.dp),
                                )
                                .padding(horizontal = 4.dp)
                                .padding(vertical = 3.dp),
                            text = state.localizer.localize("APP.GENERAL.FREE"),
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.color_purple)
                                .themeFont(fontSize = ThemeFont.FontSize.tiny),
                        )
                    }

                TransferRouteSelection.Regular ->
                    Text(
                        text = state.regularTime ?: "-",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_secondary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
            }
        }
    }

    @Composable
    private fun InstantSelectViewContent(modifier: Modifier, state: ViewState) {
        val selected = state.selection == TransferRouteSelection.Instant
        val textColor = if (selected) {
            ThemeColor.SemanticColor.text_primary
        } else {
            ThemeColor.SemanticColor.text_tertiary
        }
        val backgroundColor = if (selected) {
            ThemeColor.SemanticColor.layer_2.color
        } else {
            ThemeColor.SemanticColor.layer_4.color
        }

        val shape = RoundedCornerShape(size = 16.dp)
        Row(
            modifier = modifier
                .border(
                    width = 2.dp,
                    color = if (selected) ThemeColor.SemanticColor.color_purple.color else ThemeColor.SemanticColor.transparent.color,
                    shape = shape,
                )
                .clip(shape)
                .background(backgroundColor)
                .clickable {
                    state.selectionAction(TransferRouteSelection.Instant)
                }
                .padding(horizontal = 16.dp)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlatformImage(
                icon = R.drawable.icon_instant_deposit,
                modifier = Modifier
                    .size(20.dp),
                colorFilter = if (selected) {
                    null
                } else {
                    ColorFilter
                        .tint(ThemeColor.SemanticColor.text_tertiary.color)
                },
            )
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.INSTANT"),
                    style = TextStyle.dydxDefault
                        .themeColor(textColor)
                        .themeFont(fontSize = ThemeFont.FontSize.large),
                )
                Text(
                    text = state.instantFee ?: "",
                    style = TextStyle.dydxDefault
                        .themeColor(textColor)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            }
        }
    }

    @Composable
    private fun RegularSelectViewContent(modifier: Modifier, state: ViewState) {
        val selected = state.selection == TransferRouteSelection.Regular
        val textColor = if (selected) {
            ThemeColor.SemanticColor.text_primary
        } else {
            ThemeColor.SemanticColor.text_tertiary
        }
        val backgroundColor = if (selected) {
            ThemeColor.SemanticColor.layer_2.color
        } else {
            ThemeColor.SemanticColor.layer_4.color
        }

        val shape = RoundedCornerShape(size = 16.dp)
        Row(
            modifier = modifier
                .border(
                    width = if (selected) 2.dp else -1.dp,
                    color = ThemeColor.SemanticColor.color_purple.color,
                    shape = shape,
                )
                .clip(shape)
                .background(backgroundColor)
                .clickable {
                    state.selectionAction(TransferRouteSelection.Regular)
                }
                .padding(horizontal = 16.dp)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlatformImage(
                icon = R.drawable.icon_regular_deposit,
                modifier = Modifier
                    .size(20.dp),
                colorFilter =
                if (selected) {
                    ColorFilter.tint(ThemeColor.SemanticColor.color_purple.color)
                } else {
                    ColorFilter.tint(ThemeColor.SemanticColor.text_tertiary.color)
                },
            )
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.regularTime ?: "",
                    style = TextStyle.dydxDefault
                        .themeColor(textColor)
                        .themeFont(fontSize = ThemeFont.FontSize.large),
                )
                Text(
                    text = state.regularFee ?: "",
                    style = TextStyle.dydxDefault
                        .themeColor(textColor)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            }
        }
    }
}
