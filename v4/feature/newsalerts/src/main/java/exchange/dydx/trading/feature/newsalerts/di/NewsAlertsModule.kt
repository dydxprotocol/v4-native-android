package exchange.dydx.trading.feature.newsalerts.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxAlertsProvider
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.providers.DydxFrontendAlertsProvider
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.providers.DydxSystemAlertsProvider
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.providers.DydxTransferAlertsProvider

@Module
@InstallIn(ActivityRetainedComponent::class)
object NewsAlertsModule {
    @Provides
    @ActivityRetainedScoped
    fun provideDydxSystemAlertsProvider(
        abacusStateManager: AbacusStateManagerProtocol,
        router: DydxRouter,
        localizer: LocalizerProtocol,
    ): DydxSystemAlertsProvider {
        return DydxSystemAlertsProvider(abacusStateManager, router, localizer)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideDydxFrontendAlertsProvider(
        abacusStateManager: AbacusStateManagerProtocol,
        router: DydxRouter,
    ): DydxFrontendAlertsProvider {
        return DydxFrontendAlertsProvider(abacusStateManager, router)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideDydxTransferAlertsProvider(
        abacusStateManager: AbacusStateManagerProtocol,
        router: DydxRouter,
        localizer: LocalizerProtocol,
        formatter: DydxFormatter,
    ): DydxTransferAlertsProvider {
        return DydxTransferAlertsProvider(abacusStateManager, router, localizer, formatter)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideDydxAlertsProvider(
        dydxSystemAlertsProvider: DydxSystemAlertsProvider,
        dydxTransferAlertsProvider: DydxTransferAlertsProvider,
        dydxFrontEndAlertsProvider: DydxFrontendAlertsProvider,
    ): DydxAlertsProvider {
        return DydxAlertsProvider(dydxSystemAlertsProvider, dydxTransferAlertsProvider, dydxFrontEndAlertsProvider)
    }
}
