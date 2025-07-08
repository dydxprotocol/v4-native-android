package exchange.dydx.trading.feature.profile.help

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun Preview_DydxHelpItemView() {
    DydxThemedPreviewSurface {
        DydxHelpItemView.Content(Modifier, DydxHelpItemView.ViewState.preview)
    }
}

object DydxHelpItemView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val icon: Any? = null,
        val title: String,
        val subtitle: String,
        val onTapAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                icon = R.drawable.help_chatbot,
                title = "Help Title",
                subtitle = "Help Subtitle",
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
                .clickable(onClick = state.onTapAction ?: {})
                .fillMaxWidth()
                .clip(shape)
                .background(
                    color = ThemeColor.SemanticColor.layer_5.color,
                    shape = shape,
                )
                .border(
                    width = 1.dp,
                    color = ThemeColor.SemanticColor.layer_6.color,
                    shape = shape,
                )
                .padding(vertical = ThemeShapes.VerticalPadding)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            PlatformImage(
                icon = state.icon,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_secondary.color),
            )

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.title,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
                Text(
                    text = state.subtitle,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
            }
        }
    }
}
