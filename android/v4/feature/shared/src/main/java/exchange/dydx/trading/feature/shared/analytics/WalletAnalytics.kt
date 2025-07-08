package exchange.dydx.trading.feature.shared.analytics

import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.trading.integration.analytics.tracking.Tracking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletAnalytics @Inject constructor(
    private val tracker: Tracking,
) {
    fun logConnected(walletId: String?) {
        log(AnalyticsEvent.CONNECT_WALLET, walletId?.uppercase())
    }

    fun logDisconnected(walletId: String?) {
        log(AnalyticsEvent.DISCONNECT_WALLET, walletId?.uppercase())
    }

    private fun log(event: AnalyticsEvent, walletId: String?) {
        val wallet = CarteraConfig.shared?.wallets?.firstOrNull { it.id == walletId }
        val walletName = wallet?.userFields?.get("analyticEvent") ?: wallet?.name
        val data: Map<String, String> = mapOf(
            "walletType" to walletName,
        ).filterNotNull()

        tracker.log(
            event = event.rawValue,
            data = data,
        )
    }
}
