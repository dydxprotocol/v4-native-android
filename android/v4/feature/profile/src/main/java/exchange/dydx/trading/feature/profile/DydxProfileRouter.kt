package exchange.dydx.trading.feature.profile

import android.R.attr.path
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.ProfileRoutes
import exchange.dydx.trading.common.navigation.ProfileRoutes.debug_enable
import exchange.dydx.trading.common.navigation.dydxComposable
import exchange.dydx.trading.feature.profile.actions.debugenabled.DydxDebugEnableView
import exchange.dydx.trading.feature.profile.alerts.DydxAlertsContainerView
import exchange.dydx.trading.feature.profile.color.DydxDirectionColorPreferenceView
import exchange.dydx.trading.feature.profile.debug.DydxDebugView
import exchange.dydx.trading.feature.profile.featureflags.DydxFeatureFlagsView
import exchange.dydx.trading.feature.profile.feesstructure.DydxFeesStrcutureView
import exchange.dydx.trading.feature.profile.gastoken.DydxGasTokenView
import exchange.dydx.trading.feature.profile.help.DydxHelpView
import exchange.dydx.trading.feature.profile.history.DydxHistoryView
import exchange.dydx.trading.feature.profile.keyexport.DydxKeyExportView
import exchange.dydx.trading.feature.profile.language.DydxLanguageView
import exchange.dydx.trading.feature.profile.notifications.DydxNotificationsView
import exchange.dydx.trading.feature.profile.reportissue.DydxReportIssueView
import exchange.dydx.trading.feature.profile.rewards.DydxRewardsView
import exchange.dydx.trading.feature.profile.settings.DydxSettingsView
import exchange.dydx.trading.feature.profile.systemstatus.DydxSystemStatusView
import exchange.dydx.trading.feature.profile.theme.DydxThemeView
import exchange.dydx.trading.feature.profile.tradingnetwork.DydxTradingNetworkView
import exchange.dydx.trading.feature.profile.update.DydxUpdateView
import exchange.dydx.trading.feature.profile.userwallets.DydxUserWalletsView
import exchange.dydx.trading.feature.profile.walletsecurity.DydxWalletSecurityView
import exchange.dydx.utilities.utils.Logging

private const val TAG = "ProfileRouter"

fun NavGraphBuilder.profileGraph(
    appRouter: DydxRouter,
    logger: Logging,
) {
    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.main,
        deepLinks = appRouter.deeplinks(ProfileRoutes.main),
    ) { navBackStackEntry ->
        DydxProfileView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.settings,
        deepLinks = appRouter.deeplinks(ProfileRoutes.settings),
    ) { navBackStackEntry ->
        DydxSettingsView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.language,
        deepLinks = appRouter.deeplinks(ProfileRoutes.language),
    ) { navBackStackEntry ->
        DydxLanguageView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.theme,
        deepLinks = appRouter.deeplinks(ProfileRoutes.theme),
    ) { navBackStackEntry ->
        DydxThemeView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.env,
        deepLinks = appRouter.deeplinks(ProfileRoutes.env),
    ) { navBackStackEntry ->
        DydxTradingNetworkView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.status,
        deepLinks = appRouter.deeplinks(ProfileRoutes.status),
    ) { navBackStackEntry ->
        DydxSystemStatusView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.update,
        deepLinks = appRouter.deeplinks(ProfileRoutes.update),
    ) { navBackStackEntry ->
        DydxUpdateView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.features,
        deepLinks = appRouter.deeplinks(ProfileRoutes.features),
    ) { navBackStackEntry ->
        DydxFeatureFlagsView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.debug,
        deepLinks = appRouter.deeplinks(ProfileRoutes.debug),
    ) { navBackStackEntry ->
        DydxDebugView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.color,
        deepLinks = appRouter.deeplinks(ProfileRoutes.color),
    ) { navBackStackEntry ->
        DydxDirectionColorPreferenceView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.wallets,
        deepLinks = appRouter.deeplinks(ProfileRoutes.wallets),
    ) { navBackStackEntry ->
        DydxUserWalletsView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.key_export + "/{type}",
        arguments = listOf(navArgument("type") { type = NavType.StringType }),
        deepLinks = appRouter.deeplinks(
            destination = ProfileRoutes.key_export,
            path = "type",
        ),
    ) { navBackStackEntry ->
        val type = navBackStackEntry.arguments?.getString("type")
        if (type == null) {
            logger.e(TAG, "No type passed")
            appRouter.navigateTo(ProfileRoutes.main)
            return@dydxComposable
        }
        DydxKeyExportView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.history,
        deepLinks = appRouter.deeplinks(ProfileRoutes.history),
    ) { navBackStackEntry ->
        DydxHistoryView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.fees_structure,
        deepLinks = appRouter.deeplinks(ProfileRoutes.fees_structure),
    ) { navBackStackEntry ->
        DydxFeesStrcutureView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.help,
        deepLinks = appRouter.deeplinks(ProfileRoutes.help),
    ) { navBackStackEntry ->
        DydxHelpView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.rewards,
        deepLinks = appRouter.deeplinks(ProfileRoutes.rewards),
    ) { navBackStackEntry ->
        DydxRewardsView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.debug_enable,
        deepLinks = appRouter.deeplinks(ProfileRoutes.debug_enable),
    ) { navBackStackEntry ->
        DydxDebugEnableView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.report_issue,
        deepLinks = appRouter.deeplinks(ProfileRoutes.report_issue),
    ) { navBackStackEntry ->
        DydxReportIssueView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.notifications,
        deepLinks = appRouter.deeplinks(ProfileRoutes.notifications),
    ) { navBackStackEntry ->
        DydxNotificationsView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.gas_token,
        deepLinks = appRouter.deeplinks(ProfileRoutes.gas_token),
    ) { navBackStackEntry ->
        DydxGasTokenView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.alerts,
        deepLinks = appRouter.deeplinks(ProfileRoutes.alerts),
    ) { navBackStackEntry ->
        DydxAlertsContainerView.Content(Modifier)
    }

    dydxComposable(
        router = appRouter,
        route = ProfileRoutes.security,
        deepLinks = appRouter.deeplinks(ProfileRoutes.security),
    ) { navBackStackEntry ->
        DydxWalletSecurityView.Content(Modifier)
    }
}
