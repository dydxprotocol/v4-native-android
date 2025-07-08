package exchange.dydx.platformui.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import kotlinx.serialization.json.JsonNull.content

@Composable
fun PlatformIconButton(
    modifier: Modifier = Modifier,
    action: () -> Unit,
    backgroundColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_5,
    borderColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_6,
    size: Dp = 42.dp,
    padding: Dp = 8.dp,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(padding)
            .size(size)
            .background(backgroundColor.color, shape)
            .border(1.dp, borderColor.color, shape)
            .clip(shape)
            .clickable(
                enabled = enabled,
                onClick = action,
                role = Role.Button,
            ),
    ) {
        content()
    }

//    IconButton(
//        onClick = action,
//        modifier = Modifier
//            .padding(8.dp)
//            .size(size)
//            .background(backgroundColor, CircleShape)
//            .border(1.dp, borderColor, CircleShape)
//    ) {
//        content()
//    }
}
