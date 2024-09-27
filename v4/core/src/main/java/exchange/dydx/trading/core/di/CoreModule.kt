package exchange.dydx.trading.core.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.core.DydxRouterImpl
import exchange.dydx.trading.feature.workers.globalworkers.DydxAlertsWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxApiStatusWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxCarteraConfigWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxGasTokenWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxRestrictionsWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxTransferSubaccountWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxUpdateWorker
import exchange.dydx.trading.feature.workers.globalworkers.DydxUserTrackingWorker
import exchange.dydx.trading.integration.fcm.FCMTokenWorker
import exchange.dydx.utilities.utils.WorkerProtocol

@Module
@InstallIn(ActivityRetainedComponent::class)
interface CoreModule {

    @Binds
    fun bindDydxRouter(dydxRouterImpl: DydxRouterImpl): DydxRouter

    companion object {
        @Provides fun provideWorkers(
            dydxUpdateWorker: DydxUpdateWorker,
            dydxAlertsWorker: DydxAlertsWorker,
            dydxApiStatusWorker: DydxApiStatusWorker,
            dydxRestrictionsWorker: DydxRestrictionsWorker,
            dydxCarteraConfigWorker: DydxCarteraConfigWorker,
            dydxTransferSubaccountWorker: DydxTransferSubaccountWorker,
            dydxUserTrackingWorker: DydxUserTrackingWorker,
            dydxGasTokenWorker: DydxGasTokenWorker,
            fcmTokenWorker: FCMTokenWorker,
        ): List<WorkerProtocol> =
            listOf(
                dydxUpdateWorker,
                dydxAlertsWorker,
                dydxApiStatusWorker,
                dydxRestrictionsWorker,
                dydxCarteraConfigWorker,
                dydxTransferSubaccountWorker,
                dydxUserTrackingWorker,
                dydxGasTokenWorker,
                fcmTokenWorker,
            )
    }
}
