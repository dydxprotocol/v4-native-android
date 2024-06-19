package exchange.dydx.trading.common.navigation

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.StateFlow

/**
 * The DydxRouter is an object that lives on the Dagger DI graph but, gains access to Compose
 * routing interfaces on initialization.
 *
 * Essentially it serves as an interface between our routing logic and the underlying Compose framework.
 */
interface DydxRouter {

    enum class Presentation {
        Push,
        Modal,
        Tab,
        Default,
    }

    val initialized: StateFlow<Boolean>

    fun presentation(route: String): Presentation
    fun isParentChild(parent: String, child: String): Boolean

    fun handleIntent(intent: Intent)
    fun navigateTo(route: String, presentation: Presentation = Presentation.Default)
    fun tabTo(route: String, restoreState: Boolean = true)

    fun navigateBack()
    fun navigateToRoot(excludeRoot: Boolean)
    fun requireNavController(): NavHostController
    fun initialize(navHostController: NavHostController)

    val currentRoute: String?
    fun routeIsInBackStack(route: String): Boolean

    val destinationFlow: StateFlow<Destination?>

    data class Destination(
        val controller: NavController,
        val destination: NavDestination,
        val arguments: Bundle?,
    )

    fun deeplinks(
        destination: String,
        path: String? = null,
        params: List<String> = emptyList(),
    ): List<NavDeepLink>
}
