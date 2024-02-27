package exchange.dydx.platformui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformModalSheet(
    modifier: Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    val bottomSheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        bottomSheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = {
            //  showBottomSheet = false
        },
        sheetState = bottomSheetState,
    ) {
        // Sheet content
        content(modifier)
    }
}
