package exchange.dydx.trading.feature.transfer.search

import android.R.attr.foreground
import android.R.attr.shape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Preview
@Composable
fun Preview_DydxInstantDepositSearchItem() {
    DydxThemedPreviewSurface {
        DydxInstantDepositSearchItem.Content(Modifier, DydxInstantDepositSearchItem.ViewState.preview)
    }
}

object DydxInstantDepositSearchItem {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val token: String?,
        val chain: String?,
        val tokenIconUri: String? = null,
        val chainIconUri: String? = null,
        val tokenSize: String? = null,
        val usdcSize: String? = null,
        val isSelected: Boolean = false,
        val selectAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                token = "USDC",
                chain = "Ethereum",
                tokenIconUri = "https://v4.testnet.dydx.exchange/currencies/usdc.png",
                chainIconUri = "https://v4.testnet.dydx.exchange/chains/ethereum.png",
                tokenSize = "0.5",
                usdcSize = "2000",
                isSelected = true,
                selectAction = null,
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val shape = RoundedCornerShape(size = 16.dp)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = if (state.isSelected) ThemeColor.SemanticColor.color_purple.color else ThemeColor.SemanticColor.transparent.color,
                    shape = shape,
                )
                .clip(shape)
                .background(ThemeColor.SemanticColor.layer_3.color)
                .clickable {
                    state.selectAction?.invoke()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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

            Column(
                modifier = Modifier,
            ) {
                Text(
                    text = state.token ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_primary),
                )

                Text(
                    text = state.chain ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = state.tokenSize ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                )

                Text(
                    text = state.usdcSize ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                )
            }
        }
    }
}
