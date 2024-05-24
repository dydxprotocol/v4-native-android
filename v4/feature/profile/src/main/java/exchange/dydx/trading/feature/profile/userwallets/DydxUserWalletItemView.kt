package exchange.dydx.trading.feature.profile.userwallets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import exchange.dydx.platformui.components.buttons.PlatformAccessoryButton
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
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxUserWalletItemView() {
    DydxThemedPreviewSurface {
        DydxUserWalletItemView.Content(Modifier, DydxUserWalletItemView.ViewState.preview)
    }
}

object DydxUserWalletItemView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        var iconUrl: String? = null,
        val address: String?,
        val isSelected: Boolean,
        val explorerLinkAction: (() -> Unit)? = null,
        val exportAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                address = "0x1234567890",
                isSelected = false,
                explorerLinkAction = {},
                exportAction = {},
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val shape = RoundedCornerShape(10.dp)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    color = if (state.isSelected) {
                        ThemeColor.SemanticColor.layer_1.color
                    } else {
                        ThemeColor.SemanticColor.layer_5.color
                    },
                    shape = shape,
                )
                .border(
                    width = 1.dp,
                    color = ThemeColor.SemanticColor.layer_6.color,
                    shape = shape,
                )
                .padding(vertical = 20.dp)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            PlatformImage(
                modifier = Modifier.size(8.dp),
                icon = R.drawable.icon_status,
                colorFilter = if (state.isSelected) {
                    ColorFilter.tint(ThemeColor.SemanticColor.color_green.color)
                } else {
                    ColorFilter.tint(ThemeColor.SemanticColor.layer_1.color)
                },
            )

            PlatformRoundImage(
                icon = state.iconUrl,
                size = 36.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = state.address ?: "",
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                    maxLines = 1,
                )

                if (state.isSelected) {
                    SelectedButtons(
                        modifier = Modifier,
                        state = state,
                    )
                }
            }
        }
    }

    @Composable
    private fun SelectedButtons(
        modifier: Modifier,
        state: ViewState
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.End,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            PlatformAccessoryButton(
                action = state.explorerLinkAction ?: {},
                borderColor = ThemeColor.SemanticColor.text_tertiary,
                padding = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        maxLines = 1,
                        text = state.localizer.localize("APP.GENERAL.BLOCK_EXPLORER"),
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                    PlatformImage(
                        modifier = Modifier.size(18.dp),
                        icon = R.drawable.icon_external_link,
                        colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_tertiary.color),
                    )
                }
            }

            Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))

            PlatformAccessoryButton(
                action = state.exportAction ?: {},
                backgroundColor = ThemeColor.SemanticColor.color_faded_red,
                borderColor = ThemeColor.SemanticColor.color_red,
                padding = 0.dp,
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    maxLines = 1,
                    text = state.localizer.localize("APP.MNEMONIC_EXPORT.EXPORT_PHRASE"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.color_red)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            }
        }
    }
}
