package exchange.dydx.trading.feature.shared.scaffolds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.inputs.PlatformInputAlertState
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color

@Composable
fun InputFieldScaffold(
    modifier: Modifier = Modifier,
    alertState: PlatformInputAlertState = PlatformInputAlertState.None,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                color = ThemeColor.SemanticColor.layer_4.color,
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = alertState.borderColor.color,
                shape = shape,
            ),
    ) {
        content()
    }
}
