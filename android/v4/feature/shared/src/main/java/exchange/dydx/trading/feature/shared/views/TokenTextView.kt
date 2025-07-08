package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.textgroups.PlatformAutoSizingText
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface

@Preview
@Composable
fun Preview_TokenTextView() {
    DydxThemedPreviewSurface {
        TokenTextView.Content(Modifier, TokenTextView.ViewState.preview)
    }
}

object TokenTextView {

    data class ViewState(
        val symbol: String = "---",
    ) {
        companion object {
            val preview = ViewState(
                symbol = "ETH",
            )
        }
    }

    @Composable
    fun Content(
        modifier: Modifier,
        state: ViewState?,
        textStyle: TextStyle = TextStyle.dydxDefault,
    ) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .background(
                    color = ThemeColor.SemanticColor.layer_6.color,
                    shape = RoundedCornerShape(4.dp),
                )
                .padding(horizontal = 4.dp)
                .padding(vertical = 2.dp),
        ) {
            PlatformAutoSizingText(
                text = state.symbol,
                textStyle = textStyle,
            )
        }
    }
}
