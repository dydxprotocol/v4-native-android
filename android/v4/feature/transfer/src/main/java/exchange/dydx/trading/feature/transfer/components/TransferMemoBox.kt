package exchange.dydx.trading.feature.transfer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.alerts.PlatformInlineAlert
import exchange.dydx.platformui.components.inputs.PlatformTextInput
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold

@Preview
@Composable
fun Preview_TransferMemoBox() {
    DydxThemedPreviewSurface {
        TransferMemoBox.Content(Modifier, TransferMemoBox.ViewState.preview)
    }
}

object TransferMemoBox {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val value: String? = null,
        val showCexWarning: Boolean,
        val onEditAction: (String) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                showCexWarning = true,
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
        Column(modifier) {
            InputFieldScaffold {
                Column(
                    modifier = modifier
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(vertical = ThemeShapes.VerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = state.localizer.localize("APP.GENERAL.MEMO"),
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
                        placeHolder = state.localizer.localize("APP.DIRECT_TRANSFER_MODAL.REQUIRED_FOR_CEX"),
                        onValueChange = { state.onEditAction(it) },
                    )
                }
            }
            if (state.showCexWarning) {
                Spacer(modifier = Modifier.height(24.dp))

                PlatformInlineAlert(
                    text = state.localizer.localize("ERRORS.TRANSFER_MODAL.TRANSFER_WITHOUT_MEMO"),
                    level = PlatformInlineAlert.Level.WARNING,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
