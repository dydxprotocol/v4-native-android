package exchange.dydx.platformui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.OutlinedButton
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import kotlinx.serialization.json.JsonNull.content

@Composable
fun PlatformPillButton(
    modifier: Modifier = Modifier,
    action: () -> Unit,
    backgroundColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_5,
    borderColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_6,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        border = BorderStroke(1.dp, borderColor.color),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults
            .outlinedButtonColors(backgroundColor.color),
        enabled = enabled,
        onClick = action,
    ) {
        content()
    }
}

@Composable
fun PlatformPillItem(
    modifier: Modifier = Modifier,
    backgroundColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_5,
    borderColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_6,
    padding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(backgroundColor.color, shape)
            .border(
                width = 1.dp,
                color = borderColor.color,
                shape = shape,
            )
            .clip(shape)
            .then(modifier)
            .padding(padding),

    ) {
        content()
    }
}
