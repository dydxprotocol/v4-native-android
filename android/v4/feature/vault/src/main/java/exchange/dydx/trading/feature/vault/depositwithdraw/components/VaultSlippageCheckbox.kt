package exchange.dydx.trading.feature.vault.depositwithdraw.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.checkbox.PlatformCheckbox
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
        val text: String,
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

        PlatformCheckbox(
            modifier = modifier,
            checked = state.checked,
            textStyle = TextStyle.dydxDefault
                .themeColor(ThemeColor.SemanticColor.text_secondary)
                .themeFont(fontSize = ThemeFont.FontSize.base),
            text = AnnotatedString(state.text),
            onCheckedChange = state.onCheckedChange,
        )
    }
}
