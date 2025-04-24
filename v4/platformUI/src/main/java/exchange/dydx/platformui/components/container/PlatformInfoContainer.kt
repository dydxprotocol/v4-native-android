package exchange.dydx.platformui.components.container

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.platformui.R
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
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
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < 0) {
                                // swipe up
                                cancelAction?.invoke()
                            }
                        }
                    },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlatformImage(
                        icon = R.drawable.icon_info,
                        modifier = Modifier.size(26.dp),
                        colorFilter = ColorFilter
                            .tint(type.foregroundColor),
                    )

                    Column(Modifier.weight(1f)) {
                        title?.let {
                            Text(
                                text = it,
                                color = type.foregroundColor,
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.base, fontType = ThemeFont.FontType.plus),
                            )
                        }
                        message?.let {
                            Text(
                                text = message,
                                color = type.foregroundColor,
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.small),
                            )
                        }
                    }
                    buttonTitle?.let {
                        val shape = RoundedCornerShape(6.dp)
                        Box(
                            modifier = Modifier
                                .clip(shape)
                                .border(1.dp, type.buttonBorderColor, shape)
                                .background(type.buttonBackgroundColor)
                                .clickable(enabled = buttonAction != null) { buttonAction?.invoke() },
                        ) {
                            Text(
                                text = it,
                                color = type.buttonForegroundColor,
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

data class PlatformInfoViewModel(
    val title: String? = null,
    val message: String?,
    val buttonTitle: String? = null,
    val type: Type = Type.Info,
    val duration: Duration,
    val buttonAction: (() -> Unit)? = null,
    val cancelAction: (() -> Unit)? = null,
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

        val buttonBackgroundColor: Color
            get() = when (this) {
                Error -> ThemeColor.SemanticColor.color_white.color
                Info -> ThemeColor.SemanticColor.layer_6.color
                Warning -> ThemeColor.SemanticColor.color_white.color
            }

        val buttonForegroundColor: Color
            get() = when (this) {
                Error -> ThemeColor.SemanticColor.color_red.color
                Info -> ThemeColor.SemanticColor.text_primary.color
                Warning -> ThemeColor.SemanticColor.color_black.color
            }

        val buttonBorderColor: Color
            get() = when (this) {
                Error -> ThemeColor.SemanticColor.color_white.color
                Info -> ThemeColor.SemanticColor.layer_7.color
                Warning -> ThemeColor.SemanticColor.color_white.color
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
    private val _toasts = MutableStateFlow<PlatformInfoViewModel?>(null)
    val toasts = _toasts.asStateFlow()

    private val toastQueue = ArrayDeque<PlatformInfoViewModel>()

    private var currentJob: Job? = null

    fun show(
        title: String? = null,
        message: String? = null,
        buttonTitle: String? = null,
        type: PlatformInfoViewModel.Type = PlatformInfoViewModel.Type.Info,
        duration: PlatformInfoViewModel.Duration = PlatformInfoViewModel.Duration.Short,
        cancellable: Boolean = true,
        buttonAction: (() -> Unit)? = null,
    ) {
        toastQueue.add(
            PlatformInfoViewModel(
                title = title,
                message = message,
                buttonTitle = buttonTitle,
                type = type,
                duration = duration,
                cancelAction = {
                    if (cancellable) {
                        currentJob?.cancel()
                    }
                },
                buttonAction = {
                    buttonAction?.invoke()
                    currentJob?.cancel()
                },
            ),
        )
        if (currentJob == null || currentJob?.isCompleted == true) {
            displayNextToast()
        }
    }

    private fun displayNextToast() {
        if (toastQueue.isNotEmpty()) {
            val nextToast: PlatformInfoViewModel
            try {
                nextToast = toastQueue.removeFirst()
                _toasts.value = nextToast
            } catch (e: NoSuchElementException) {
                _toasts.value = null
                return
            }

            currentJob = appScope.launch {
                try {
                    delay(nextToast.duration.millis)
                    _toasts.value = null
                    displayNextToast()
                } catch (e: CancellationException) {
                    _toasts.value = null
                    appScope.launch {
                        delay(500)
                        displayNextToast()
                    }
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
