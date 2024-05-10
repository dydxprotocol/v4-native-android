package exchange.dydx.trading.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import exchange.dydx.abacus.protocols.LoggingProtocol
import exchange.dydx.feature.onboarding.loginGraph
import exchange.dydx.newsalerts.newsAlertsGraph
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.feature.market.marketGraph
import exchange.dydx.trading.feature.portfolio.portfolioGraph
import exchange.dydx.trading.feature.profile.profileGraph
import exchange.dydx.trading.feature.trade.tradeGraph
import exchange.dydx.trading.feature.transfer.transferGraph
import timber.log.Timber

private const val TAG = "DydxNavGraph"

private const val DEFAULT_START_DESTINATION = PortfolioRoutes.main

/**
 * This is the heart of how navigation is defined in Compose.
 *
 * In "InitializeManagers" we start some important coroutines that should run anytime the UI is attached.
 *
 * Then we define the various states our app can be in.
 *
 * Our graph is decomposed into module specific subgraphs.
 */
@Composable
fun DydxNavGraph(
    appRouter: DydxRouter,
    modifier: Modifier = Modifier,
    logger: LoggingProtocol,
) {
    val navController: NavHostController = rememberNavController()
    appRouter.initialize(navController)

    InitializeManagers()

    NavHost(
        navController = navController,
        startDestination = DEFAULT_START_DESTINATION,
        modifier = modifier,
    ) {
        loginGraph(
            appRouter = appRouter,
            logger = logger,
        )

        marketGraph(
            appRouter = appRouter,
            logger = logger,
        )

        tradeGraph(
            appRouter = appRouter,
            logger = logger,
        )

        profileGraph(
            appRouter = appRouter,
            logger = logger,
        )

        newsAlertsGraph(
            appRouter = appRouter,
            logger = logger,
        )

        portfolioGraph(
            appRouter = appRouter,
            logger = logger,
        )

        transferGraph(
            appRouter = appRouter,
            logger = logger,
        )
    }
}

@Composable
private fun InitializeManagers(
    coreViewModel: CoreViewModel = hiltViewModel(),
) {
    // Will stay connected until restartCount changes
    // increment restartCount to cancel all downstream coroutines and
    // manually reconnect
    LaunchedEffect(coreViewModel.restartCount) {
        coreViewModel.logger.d(TAG, "Intializing core services")
        coreViewModel.start()
    }
}
