package exchange.dydx.trading.common.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import kotlinx.serialization.json.JsonNull.content

object DydxAnimation {
    private val durationMillis = 400
    private val easing: Easing = FastOutSlowInEasing

    fun enterScreen(router: DydxRouter, route: String): ((@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)) =
        {
            val initial = this.initialState.destination.route
            if (initial != null && router.isParentChild(parent = route, child = initial)) {
                // A <- B where A is entering
                when (router.presentation(initial)) {
                    DydxRouter.Presentation.Modal -> null
                    DydxRouter.Presentation.Push -> enterToRight()
                    else -> null
                }
            } else {
                // A -> B where B is entering
                when (router.presentation(route)) {
                    DydxRouter.Presentation.Modal -> modalEnter()
                    DydxRouter.Presentation.Push -> enterToLeft()
                    else -> null
                }
            }
        }

    fun exitScreen(router: DydxRouter, route: String): ((@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)) =
        {
            val target = this.targetState.destination.route
            if (target != null && target != route) {
                val targetPresentation = router.presentation(target)
                val presentation = router.presentation(route)
                if (router.isParentChild(parent = target, child = route)) {
                    // A <- B where B is exiting
                    when (presentation) {
                        DydxRouter.Presentation.Modal -> modalExit()
                        DydxRouter.Presentation.Push -> exitToRight()
                        else -> null
                    }
                } else if (router.isParentChild(parent = route, child = target)) {
                    // A -> B where A is exiting
                    when (targetPresentation) {
                        DydxRouter.Presentation.Modal -> null
                        DydxRouter.Presentation.Push -> exitToLeft()
                        else -> null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }

    private val enterToLeft: ((@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)) = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = 0,
                easing = easing,
            ),
        )
    }

    private val enterToRight: ((@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)) = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = 0,
                easing = easing,
            ),
        )
    }

    private val exitToLeft: ((@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)) = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = 0,
                easing = easing,
            ),
        )
    }

    private val exitToRight: ((@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)) = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = 0,
                easing = easing,
            ),
        )
    }

    private val modalEnter: ((@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)) = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = 0,
                easing = easing,
            ),
        )
    }

    private val modalExit: ((@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)) = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = 0,
                easing = easing,
            ),
        )
    }

    @Composable
    fun AnimateFadeInOut(
        visible: Boolean,
        content: @Composable AnimatedVisibilityScope.() -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
            content = content,
        )
    }

    @Composable
    fun AnimateExpandInOut(
        visible: Boolean,
        content: @Composable AnimatedVisibilityScope.() -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = expandVertically(),
            exit = shrinkVertically(),
            content = content,
        )
    }
}
