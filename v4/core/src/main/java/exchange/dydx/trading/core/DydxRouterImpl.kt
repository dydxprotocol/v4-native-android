package exchange.dydx.trading.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.DydxRouter.Destination
import exchange.dydx.trading.common.navigation.MarketRoutes
import exchange.dydx.trading.integration.analytics.Tracking
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

private const val TAG = "DydxRouterImpl"

/**
 * While the DxdxRouter interface is "common" and can be used by any module.
 *
 * The implementation is "core" and can see any module.
 *
 * So this is a very powerful class in terms of, bridging the dependency tree.
 */
class DydxRouterImpl(
    override var androidContext: Context?,
    private val appConfig: AppConfig,
    private val tracker: Tracking,
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
        "${appConfig.appScheme}://${appConfig.appSchemeHost}",
    )

    private val destinationChangedListener: (controller: NavController, destination: NavDestination, arguments: Bundle?) -> Unit =
        { controller, destination, arguments ->
            val dest = Destination(controller, destination, arguments)
            val success = destinationFlow.tryEmit(dest)
            if (!success) {
                Timber.tag(TAG).w("Failed to emit: %s", dest.toString())
            }

            val destinationRoute = destination.route
            if (destinationRoute != null) {

                val trackingData: MutableMap<String, String> = mutableMapOf()
                destination.arguments.keys.forEach { key ->
                    trackingData[key] = arguments?.getString(key) ?: ""
                }
                tracker.log(
                    event = destinationRoute,
                    data = trackingData,
                )

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
            } else {
                Timber.tag(TAG).w("Destination route is null")
            }
        }

    override fun initialize(navHostController: NavHostController) {
        Timber.tag(TAG).d("DydxRouter initialized")
        this.navHostController = navHostController
        navHostController.addOnDestinationChangedListener(destinationChangedListener)
    }

    override val destinationFlow: MutableStateFlow<Destination?> = MutableStateFlow(null)

    override fun handleIntent(intent: Intent) {
        // any internal intent routing logic can go here
    }

    override fun navigateTo(route: String, presentation: DydxRouter.Presentation) {
        val routePath = routePath(route)
        if (routePath.startsWith("http://") || routePath.startsWith("https://")) {
            val intent = Intent(Intent.ACTION_VIEW, routePath.toUri())
            androidContext?.startActivity(intent)
        } else {
            pendingPresentation =
                presentation // Let destinationChangedListener handle the presentation state change
            try {
                navHostController.navigate(routePath) {
                    launchSingleTop = true
                    restoreState = false
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to navigate to route: %s", routePath)
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
        routeQueue.removeLast()
        navHostController?.popBackStack()
    }

    override fun requireNavController(): NavHostController {
        return this.navHostController
    }

    override val currentRoute: String?
        get() = navHostController.currentDestination?.route

    override fun routeIsInBackStack(route: String): Boolean {
        return routeQueue.contains(route)
    }

    override fun deeplinks(path: String): List<NavDeepLink> {
        return dydxUris.map { uri ->
            navDeepLink {
                uriPattern = "$uri/$path"
            }
        }
    }

    override fun deeplinksWithParam(
        destination: String,
        param: String,
        isPath: Boolean,
    ): List<NavDeepLink> {
        val baseLinks = dydxUris.map { uri -> "$uri/$destination" }

        return if (isPath) {
            baseLinks.map { uri -> navDeepLink { uriPattern = "$uri/{$param}" } }
        } else {
            baseLinks.map { uri -> navDeepLink { uriPattern = "$uri?$param={$param}" } }
        }
    }

    private fun routePath(route: String): String {
        var route = route
        for (url in dydxUris) {
            if (route.startsWith(url)) {
                route = route.replace(url, "")
            }
        }
        if (route.startsWith("/")) {
            return route.substring(1)
        }
        return route
    }
}
