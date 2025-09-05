package exchange.dydx.feature.onboarding

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.feature.onboarding.connect.DydxOnboardConnectView
import exchange.dydx.feature.onboarding.debugscan.DydxDebugScanView
import exchange.dydx.feature.onboarding.depositprompt.DydxDepositPromptView
import exchange.dydx.feature.onboarding.desktopscan.DydxDesktopScanView
import exchange.dydx.feature.onboarding.tos.DydxTosView
import exchange.dydx.feature.onboarding.turnkey.DydxTurnkeyAuthView
import exchange.dydx.feature.onboarding.walletlist.DydxWalletListView
import exchange.dydx.feature.onboarding.welcome.DydxOnboardWelcomeView
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.utilities.utils.Logging

private const val TAG: String = "LoginRoute"

fun NavGraphBuilder.loginGraph(
    appRouter: DydxRouter,
    logger: Logging,
) {
    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.deposit_prompt,
        deepLinks = appRouter.deeplinks(OnboardingRoutes.deposit_prompt),
    ) { nbse ->
        DydxDepositPromptView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.welcome,
        deepLinks = appRouter.deeplinks(OnboardingRoutes.welcome),
    ) { nbse ->
        DydxOnboardWelcomeView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.turnkey + "?token={token}",
        arguments = listOf(
            navArgument("token") {
                type = NavType.StringType
                nullable = true
            },
        ),
        deepLinks = appRouter.deeplinks(destination = OnboardingRoutes.turnkey, params = listOf("token")),
    ) { nbse ->
        DydxTurnkeyAuthView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.wallet_list + "?mobileOnly={mobileOnly}&backButtonRoute={backButtonRoute}",
        arguments = listOf(
            navArgument("mobileOnly") {
                type = NavType.StringType
                defaultValue = "false"
                nullable = true
            },
            navArgument("backButtonRoute") {
                type = NavType.StringType
                nullable = true
            },
        ),
        deepLinks = appRouter.deeplinks(destination = OnboardingRoutes.wallet_list, params = listOf("mobileOnly", "backButtonRoute")),
    ) { nbse ->
        DydxWalletListView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.connect + "/{walletId}",
        arguments = listOf(
            navArgument("walletId") {
                type = NavType.StringType
            },
        ),
        deepLinks = appRouter.deeplinks(OnboardingRoutes.connect, "walletId"),
    ) { nbse ->
        DydxOnboardConnectView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.desktop_scan + "?backButtonRoute={backButtonRoute}",
        arguments = listOf(
            navArgument("backButtonRoute") {
                type = NavType.StringType
                nullable = true
            },
        ),
        deepLinks = appRouter.deeplinks(OnboardingRoutes.desktop_scan),
    ) { nbse ->
        DydxDesktopScanView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.debug_scan,
        deepLinks = appRouter.deeplinks(OnboardingRoutes.debug_scan),
    ) { nbse ->
        DydxDebugScanView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = OnboardingRoutes.tos,
        deepLinks = appRouter.deeplinks(OnboardingRoutes.tos),
    ) { nbse ->
        DydxTosView.Content(Modifier)
    }
}
