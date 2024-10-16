package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_BuySellView() {
    DydxThemedPreviewSurface {
        BuySellView.Content(Modifier, BuySellView.ViewState.preview)
    }
}

object BuySellView {
    sealed class ButtonType(
        val borderWidth: Dp,
    ) {
        object Primary : ButtonType(2.dp)
        object Secondary : ButtonType(1.dp)
    }

    private val optionHeight: Dp = 44.dp
    private val cornerRadius: Dp = 10.dp

    data class ViewState(
        val localizer: LocalizerProtocol,
        val text: String? = null,
        val color: ThemeColor.SemanticColor = ThemeColor.SemanticColor.positiveColor,
        val buttonType: ButtonType = ButtonType.Primary,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                text = "Buy",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val backgroundColor = when (state.buttonType) {
            is ButtonType.Primary -> ThemeColor.SemanticColor.layer_2.color
            is ButtonType.Secondary -> ThemeColor.SemanticColor.layer_4.color
        }
        val borderColor = when (state.buttonType) {
            is ButtonType.Primary -> state.color
            is ButtonType.Secondary -> ThemeColor.SemanticColor.layer_6
        }
        val shape = RoundedCornerShape(size = cornerRadius)
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
                .height(optionHeight)
                .background(backgroundColor, shape)
                .border(
                    width = state.buttonType.borderWidth,
                    color = borderColor.color,
                    shape = shape,
                )
                .clip(shape)
                .then(modifier),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.medium, fontType = ThemeFont.FontType.plus)
                    .themeColor(foreground = state.color),
                text = state.text ?: "",
                maxLines = 1,
            )
        }
    }
}
