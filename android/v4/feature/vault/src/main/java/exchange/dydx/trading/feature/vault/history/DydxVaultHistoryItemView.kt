package exchange.dydx.trading.feature.vault.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
fun Preview_DydxVaultHistoryItemView() {
    DydxThemedPreviewSurface {
        DydxVaultHistoryItemView.Content(Modifier, DydxVaultHistoryItemView.ViewState.preview)
    }
}

object DydxVaultHistoryItemView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val date: String? = null,
        val time: String? = null,
        val action: String? = null,
        val amount: String? = null,
        val onTapAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                date = "2021-01-01",
                time = "12:00:00",
                action = "Deposit",
                amount = "$1,000.00",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { state.onTapAction?.invoke() }
                .padding(horizontal = 8.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = state.date ?: "-",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
                Text(
                    text = state.time ?: "-",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = state.action ?: "-",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = state.amount ?: "-",
                    textAlign = TextAlign.End,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
                Spacer(modifier = Modifier.size(8.dp))
                PlatformImage(
                    icon = R.drawable.icon_external_link,
                    modifier = Modifier.size(12.dp),
                    colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_secondary.color),
                )
            }
        }
    }
}
