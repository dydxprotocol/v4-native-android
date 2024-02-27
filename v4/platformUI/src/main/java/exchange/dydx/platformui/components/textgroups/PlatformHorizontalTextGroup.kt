package exchange.dydx.platformui.components.textgroups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

data class TextPair(
    val title: String?,
    val text: String?,
    val titleStyle: TextStyle = TextStyle.dydxDefault
        .themeColor(ThemeColor.SemanticColor.text_tertiary)
        .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.small),
    val textStyle: TextStyle = TextStyle.dydxDefault
        .themeColor(ThemeColor.SemanticColor.text_primary)
        .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.small),
) {
    companion object {
        val preview = TextPair(
            title = "Title",
            text = "Text",
        )
    }
}

@Composable
fun PlatformHorizontalTextGroup(
    modifier: Modifier = Modifier,
    viewState: TextPair,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
    ) {
        viewState.title?.let {
            Text(
                text = it,
                modifier = modifier,
                style = viewState.titleStyle,
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        viewState.text?.let {
            Text(
                text = it,
                modifier = modifier,
                style = viewState.textStyle,
            )
        }
    }
}
