package exchange.dydx.trading.feature.shared.scarfolds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color

@Composable
fun InputFieldScarfold(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .background(color = ThemeColor.SemanticColor.layer_4.color, shape = shape)
            .clip(shape)
            .border(
                width = 1.dp,
                color = ThemeColor.SemanticColor.layer_6.color,
                shape = shape,
            ),
    ) {
        content()
    }
}
