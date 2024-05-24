package exchange.dydx.trading.feature.transfer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
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
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold

@Preview
@Composable
fun Preview_ChainsComboBox() {
    DydxThemedPreviewSurface {
        ChainsComboBox.Content(Modifier, ChainsComboBox.ViewState.preview)
    }
}

object ChainsComboBox {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val text: String? = null,
        val label: String? = null,
        val icon: Any? = null,
        val onTapAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                text = "Text",
                label = "Label",
                icon = R.drawable.status_error,
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        InputFieldScaffold(
            modifier = modifier.then(
                state.onTapAction?.let {
                    Modifier.clickable(onClick = it)
                } ?: Modifier,
            ),
        ) {
            Row(
                modifier = modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = ThemeShapes.VerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = state?.label ?: "",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.mini),
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlatformRoundImage(
                            icon = state?.icon,
                            size = 20.dp,
                        )
                        Text(
                            text = state?.text ?: "",
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_primary)
                                .themeFont(fontSize = ThemeFont.FontSize.medium),
                        )
                    }
                }
                if (state.onTapAction != null) {
                    PlatformImage(
                        modifier = Modifier.size(10.dp),
                        icon = R.drawable.icon_triangle_down,
                        colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_tertiary.color),
                    )
                }
            }
        }
    }
}
