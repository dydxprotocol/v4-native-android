package exchange.dydx.platformui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull.content

@Composable
fun PlatformInfoScaffold(
    modifier: Modifier,
    platformInfo: PlatformInfo,
    content: @Composable (Modifier) -> Unit,
) {
    val infoType = platformInfo.infoType.collectAsState().value

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = platformInfo.snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth(),
            ) { data ->
                Snackbar(
                    backgroundColor = infoType.backgroundColor,
                    contentColor = infoType.foregroundColor,
                    actionColor = infoType.foregroundColor,
                    snackbarData = data,
                )
            }
        },
        backgroundColor = ThemeColor.SemanticColor.layer_2.color,
        modifier = Modifier,
    ) { contentPadding ->
        content(modifier.padding(contentPadding))
    }
}

data class PlatformInfo(
    internal val snackbarHostState: SnackbarHostState,
    internal val infoType: MutableStateFlow<InfoType>
) {
    enum class InfoType {
        Error, Info, Warning;

        val backgroundColor: Color
            get() = when (this) {
                Error -> ThemeColor.SemanticColor.color_red.color
                Info -> ThemeColor.SemanticColor.color_purple.color
                Warning -> ThemeColor.SemanticColor.color_yellow.color
            }

        val foregroundColor: Color
            get() = when (this) {
                Error -> ThemeColor.SemanticColor.color_white.color
                Info -> ThemeColor.SemanticColor.color_white.color
                Warning -> ThemeColor.SemanticColor.color_black.color
            }
    }

    fun show(
        title: String? = null,
        message: String,
        buttonTitle: String? = null,
        type: InfoType = InfoType.Info,
        buttonAction: (() -> Unit)? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        infoType.value = type
        CoroutineScope(Dispatchers.Main).launch {
            val result = snackbarHostState.showSnackbar(
                message = if (title.isNullOrBlank()) message else title + "\n" + message,
                actionLabel = buttonTitle,
                duration = duration,
            )
            if (result == SnackbarResult.ActionPerformed) {
                buttonAction?.invoke()
            }
        }
    }
}
