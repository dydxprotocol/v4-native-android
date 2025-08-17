package exchange.dydx.trading.core

import android.R.attr.path
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.DydxRouter.Destination
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.feature.shared.analytics.RoutingAnalytics
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

private const val TAG = "DydxRouterImpl"

/**
 * While the DxdxRouter interface is "common" and can be used by any module.
 *
 * The implementation is "core" and can see any module.
 *
 * So this is a very powerful class in terms of, bridging the dependency tree.
 */
@ActivityRetainedScoped
class DydxRouterImpl @Inject constructor(
    private val application: Application,
    private val tracker: Tracking,
    appConfig: AppConfig,
    private val logger: Logging,
    private val routingAnalytics: RoutingAnalytics,
) : DydxRouter {

    private lateinit var navHostController: NavHostController

    private val routeQueue = mutableListOf<String>()
    private val tabRoutes = mutableListOf<String>(
        MarketRoutes.marketList,
    )

    private val presentationMap = mutableMapOf<String, DydxRouter.Presentation>()
    private val parentMap = mutableMapOf<String, String>()

    private var pendingPresentation: DydxRouter.Presentation? = null

    override fun presentation(route: String): DydxRouter.Presentation {
        return presentationMap[route] ?: DydxRouter.Presentation.Default
    }

    override fun isParentChild(parent: String, child: String): Boolean {
        return parentMap[child] == parent
    }

    private val dydxUris: List<String> = listOf(
        "https://${appConfig.appWebHost}",
        "${appConfig.appScheme}://",
        //    "${appConfig.appScheme}://${appConfig.appSchemeHost}",
    )

    // All routes paths that are used for deeplinking
    // This should match what's declared as intent-filters in the AndroidManifest
    private val deeplinkRoutes: List<String> = listOf(
        "markets",
        "market",
        "portfolio",
        "settings",
        "onboard",
        "rewards",
        "action",
        "transfer",
    )

    private val destinationChangedListener: (controller: NavController, destination: NavDestination, arguments: Bundle?) -> Unit =
        { controller, destination, arguments ->
            val destinationRoute = destination.route
            if (destinationRoute != null) {
                routingAnalytics.logRoute(destinationRoute, arguments)

                if (tabRoutes.contains(destinationRoute)) {
                    routeQueue.clear()
                }

                if (routeQueue.contains(destinationRoute)) {
                    routeQueue.indexOf(destinationRoute).let { index ->
                        routeQueue.subList(index + 1, routeQueue.size).clear()
                    }
                } else {
                    routeQueue.add(destinationRoute)
                }

                pendingPresentation?.let {
                    presentationMap[destinationRoute] = it
                    if (routeQueue.size > 1) {
                        parentMap[destinationRoute] = routeQueue[routeQueue.size - 2]
                    }
                    pendingPresentation = null
                }

                val dest = Destination(controller, destination, arguments)
                val success = destinationFlow.tryEmit(dest)
                if (!success) {
                    logger.e(TAG, "Failed to emit: $dest")
                }
            } else {
                logger.e(TAG, "Destination route is null")
            }
        }

    override fun initialize(navHostController: NavHostController) {
        logger.d(TAG, "DydxRouter initialized")
        this.navHostController = navHostController
        navHostController.addOnDestinationChangedListener(destinationChangedListener)
        _initialized.value = true
    }

    private val _initialized = MutableStateFlow(false)
    override val initialized: StateFlow<Boolean> = _initialized

    override val destinationFlow: MutableStateFlow<Destination?> = MutableStateFlow(null)

    override fun handleIntent(intent: Intent) {
        if (this::navHostController.isInitialized) {
            val handled = navHostController.handleDeepLink(intent)
            if (!handled) {
                logger.d(TAG, "Intent not handled by NavHostController: ${intent.action}")
            }
        }
    }

    override fun navigateTo(route: String, presentation: DydxRouter.Presentation) {
        val routePath = routePath(route)
        if (routePath.startsWith("http://") || routePath.startsWith("https://")) {
            val intent = Intent(Intent.ACTION_VIEW, routePath.toUri())
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
        } else {
            pendingPresentation =
                presentation // Let destinationChangedListener handle the presentation state change
            try {
                navHostController.navigate(routePath) {
                    launchSingleTop = true
                    restoreState = false
                }
            } catch (e: Exception) {
                logger.e(TAG, "Failed to navigate to route: $routePath, error: ${e.message}")
            }
        }
    }

    override fun tabTo(route: String, restoreState: Boolean) {
        if (route !in tabRoutes) {
            tabRoutes.add(route)
        }
        routeQueue.clear()

        presentationMap[route] = DydxRouter.Presentation.Tab

        //  val inclusive = rootRoute != route
        navHostController.navigate(route) {
            popUpTo(navHostController.graph.findStartDestination().id) {
                saveState = saveState
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            this.restoreState = restoreState
        }
    }

    override fun navigateBack() {
        routeQueue.removeLastOrNull()?.also {
            navHostController.popBackStack()
        }
    }

    override fun navigateToRoot(excludeRoot: Boolean) {
        routeQueue.clear()
        navHostController.popBackStack(
            destinationId = navHostController.graph.findStartDestination().id,
            inclusive = excludeRoot,
        )
    }

    override fun requireNavController(): NavHostController {
        return this.navHostController
    }

    override val currentRoute: String?
        get() = navHostController.currentDestination?.route

    override fun routeIsInBackStack(route: String): Boolean {
        return routeQueue.contains(route)
    }

    override fun deeplinks(
        destination: String,
        path: String?,
        params: List<String>,
    ): List<NavDeepLink> {
        val baseLinks = dydxUris.map { uri -> "$uri/$destination" }

        var paramsString = ""
        for (moreParam in params) {
            if (paramsString.isNotEmpty()) {
                paramsString += "&"
            }
            paramsString += "$moreParam={$moreParam}"
        }

        return if (path.isNullOrBlank().not()) {
            baseLinks.map { uri ->
                navDeepLink {
                    uriPattern =
                        if (paramsString.isNotEmpty()) {
                            "$uri/{$path}?$paramsString"
                        } else {
                            "$uri/{$path}"
                        }
                }
            }
        } else {
            baseLinks.map { uri ->
                navDeepLink {
                    uriPattern =
                        if (paramsString.isNotEmpty()) {
                            "$uri?$paramsString"
                        } else {
                            uri
                        }
                }
            }
        }
    }

    private fun routePath(route: String): String {
        val route = trimUrlHead(route)
        return if (route.startsWith("/")) route.substring(1) else route
    }

    private fun trimUrlHead(route: String): String {
        // Remove the url head from the route if the route is a deeplink the app supports
        // For example, if the route is "https://{app_web_host]/markets", return "/markets"
        for (url in dydxUris) {
            for (path in deeplinkRoutes) {
                val urlPath = "$url/$path"
                if (route.startsWith(urlPath)) {
                    return route.replace(url, "")
                }
            }
        }
        return route
    }
}
