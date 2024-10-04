package exchange.dydx.trading.feature.vault.depositwithdraw.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformSelectedIcon
import exchange.dydx.platformui.components.icons.PlatformUnselectedIcon
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_VaultSlippageCheckbox() {
    DydxThemedPreviewSurface {
        VaultSlippageCheckbox.Content(Modifier, VaultSlippageCheckbox.ViewState.preview)
    }
}

object VaultSlippageCheckbox {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val text: String?,
        val checked: Boolean = false,
        val onCheckedChange: (Boolean) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                text = "1.0M",
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
                .clickable {
                    state.onCheckedChange(!state.checked)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.checked) {
                PlatformSelectedIcon(size = 24.dp)
            } else {
                PlatformUnselectedIcon(size = 24.dp)
            }

            Text(
                text = state.text ?: "",
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_secondary)
                    .themeFont(fontSize = ThemeFont.FontSize.base),
                modifier = Modifier.padding(start = 8.dp).weight(1f),
            )
        }
    }
}
