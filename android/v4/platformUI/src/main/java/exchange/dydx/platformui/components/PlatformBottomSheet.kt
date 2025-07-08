package exchange.dydx.platformui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformBottomSheet(
    modifier: Modifier,
    backgroundColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_3,
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    sheetContent: @Composable ColumnScope.() -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    BottomSheetScaffold(
        sheetContent = sheetContent,
        scaffoldState = scaffoldState,
        modifier = modifier,
        sheetPeekHeight = sheetPeekHeight,
        sheetContainerColor = backgroundColor.color,
        sheetShape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
        ),
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                color = ThemeColor.SemanticColor.layer_6.color,
            )
        },
    ) {
        content(modifier)
    }
}
