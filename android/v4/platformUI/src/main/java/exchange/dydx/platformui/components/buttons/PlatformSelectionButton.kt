package exchange.dydx.platformui.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color

@Composable
fun PlatformSelectionButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    content: @Composable () -> Unit,
) {
    val backgroundColor = if (selected) {
        ThemeColor.SemanticColor.layer_2.color
    } else {
        ThemeColor.SemanticColor.layer_5.color
    }

    val shape = RoundedCornerShape(size = 8.dp)
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(backgroundColor, shape)
            .border(1.dp, ThemeColor.SemanticColor.layer_6.color, shape)
            .clip(shape)
            .then(modifier)
            .padding(11.dp),
    ) {
        content()
    }
}
