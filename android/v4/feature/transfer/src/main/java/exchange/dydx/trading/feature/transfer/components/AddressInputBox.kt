package exchange.dydx.trading.feature.transfer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.platformui.components.inputs.PlatformTextInput
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold

@Preview
@Composable
fun Preview_AddressInputBox() {
    DydxThemedPreviewSurface {
        AddressInputBox.Content(Modifier, AddressInputBox.ViewState.preview)
    }
}

object AddressInputBox {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val parser: ParserProtocol,
        val value: String? = null,
        val placeholder: String? = null,
        val onEditAction: (String) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                parser = Parser(),
                placeholder = "0x1234567890",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
        InputFieldScaffold(modifier) {
            Column(
                modifier = modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = ThemeShapes.VerticalPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.DESTINATION"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.mini),
                )

                PlatformTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.value ?: "",
                    textStyle = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                    placeHolder = state.placeholder ?: "",
                    onValueChange = { state.onEditAction(it) },
                )
            }
        }
    }
}
