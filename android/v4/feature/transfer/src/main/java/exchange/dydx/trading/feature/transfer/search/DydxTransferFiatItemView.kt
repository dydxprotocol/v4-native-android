package exchange.dydx.trading.feature.transfer.search

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
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxTransferFiatItemView() {
    DydxThemedPreviewSurface {
        DydxTransferFiatItemView.Content(Modifier, DydxTransferFiatItemView.ViewState.preview)
    }
}

object DydxTransferFiatItemView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val selectAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
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
                    color = ThemeColor.SemanticColor.transparent.color,
                    shape = shape,
                )
                .clip(shape)
                .clickable {
                    state.selectAction?.invoke()
                }
                .background(ThemeColor.SemanticColor.layer_3.color)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlatformImage(
                icon = R.drawable.icon_cash,
                modifier = Modifier
                    .size(26.dp),
            )

            Column(
                modifier = Modifier,
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.DEPOSIT_WITH_FIAT"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_primary),
                )

                Text(
                    text = state.localizer.localize("APP.ONBOARDING.DEBIT") + ", " +
                        state.localizer.localize("APP.ONBOARDING.CREDIT_CARD") + ", etc",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Icon(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = "",
                    modifier = Modifier.size(16.dp),
                    tint = ThemeColor.SemanticColor.text_secondary.color,
                )
            }
        }
    }
}
