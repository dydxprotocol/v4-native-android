package exchange.dydx.platformui.components.container

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.di.CoroutineScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun PlatformInfoContainer(
    modifier: Modifier = Modifier,
) {
    val toastsViewModel: ToastContainerViewModel = hiltViewModel()
    val toast by toastsViewModel.toaster.toasts.collectAsState()
    AnimatedContent(
        targetState = toast,
        label = "ToastAnimation",
        modifier = modifier,
        transitionSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down) togetherWith slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Up,
            )
        },
    ) { state ->
        state?.run {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(type.backgroundColor)
                    .clickable(enabled = buttonAction != null) { buttonAction?.invoke() },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Column(Modifier.weight(1f)) {
                        title?.let {
                            Text(
                                text = it,
                                color = type.foregroundColor,
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.base, fontType = ThemeFont.FontType.plus),
                            )
                        }
                        Text(
                            text = message,
                            color = type.foregroundColor,
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.small),
                        )
                    }
                    buttonTitle?.let {
                        Box(Modifier.border(width = 1.dp, color = ThemeColor.SemanticColor.layer_7.color, shape = RoundedCornerShape(6.dp))) {
                            Text(
                                text = it,
                                color = type.foregroundColor,
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.small),
                                modifier =
                                Modifier
                                    .padding(10.dp),
                            )
                        }
                    }
                }
            }
        } ?: Box(Modifier.fillMaxSize())
    }
}

data class Toast(
    val title: String? = null,
    val message: String,
    val buttonTitle: String? = null,
    val type: Type = Type.Info,
    val duration: Duration,
    val buttonAction: (() -> Unit)? = null,
) {
    enum class Type {
        Error, Info, Warning;

        val backgroundColor: Color
            get() = when (this) {
                Error -> ThemeColor.SemanticColor.color_red.color
                Info -> ThemeColor.SemanticColor.layer_5.color
                Warning -> ThemeColor.SemanticColor.color_yellow.color
            }

        val foregroundColor: Color
            get() = when (this) {
                Error -> ThemeColor.SemanticColor.color_white.color
                Info -> ThemeColor.SemanticColor.text_primary.color
                Warning -> ThemeColor.SemanticColor.color_black.color
            }
    }

    enum class Duration(val millis: kotlin.Long) {
        Short(4_000L), Long(10_00L), Indefinite(kotlin.Long.MAX_VALUE)
    }
}

@Singleton
class PlatformInfo @Inject constructor(
    @CoroutineScopes.App val appScope: CoroutineScope,
) {
    private val _toasts = MutableStateFlow<Toast?>(null)
    val toasts = _toasts.asStateFlow()

    private val toastQueue = ArrayDeque<Toast>()

    private var currentJob: Job? = null

    fun show(
        title: String? = null,
        message: String,
        buttonTitle: String? = null,
        type: Toast.Type = Toast.Type.Info,
        duration: Toast.Duration = Toast.Duration.Short,
        buttonAction: (() -> Unit)? = null,
    ) {
        toastQueue.add(
            Toast(title, message, buttonTitle, type, duration, buttonAction),
        )
        if (currentJob == null || currentJob?.isCompleted == true) {
            displayNextToast()
        }
    }

    private fun displayNextToast() {
        if (toastQueue.isNotEmpty()) {
            val nextToast = toastQueue.removeFirst()
            _toasts.value = nextToast

            currentJob = appScope.launch {
                try {
                    delay(nextToast.duration.millis)
                    _toasts.value = null
                    displayNextToast()
                } catch (e: CancellationException) {
                    _toasts.value = null
                }
            }
        }
    }
}

// Purely exists as a DI hook for Compose
@HiltViewModel
internal class ToastContainerViewModel @Inject constructor(
    val toaster: PlatformInfo,
) : ViewModel()