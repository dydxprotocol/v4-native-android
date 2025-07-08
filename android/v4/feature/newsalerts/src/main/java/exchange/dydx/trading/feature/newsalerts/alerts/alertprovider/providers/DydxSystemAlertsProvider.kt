package exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.providers

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.ApiState
import exchange.dydx.abacus.state.manager.ApiStatus
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.newsalerts.alerts.DydxAlertsView
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.AlertType
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxAlertsProviderItemProtocol
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxCustomAlertsProviderProtocol
import exchange.dydx.trading.feature.shared.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

@ActivityRetainedScoped
class DydxSystemAlertsProvider @Inject constructor(
    private val abacusStateManger: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val localizer: LocalizerProtocol,
) : DydxCustomAlertsProviderProtocol {

    private var hadError: Boolean = false

    override val alertType: AlertType = AlertType.System

    override val items: Flow<List<DydxAlertsProviderItemProtocol>> =
        abacusStateManger.state.apiState.map {
            val item = createApiStatusAlertItem(it)
            if (item != null) {
                listOf(item)
            } else {
                emptyList()
            }
        }
            .distinctUntilChanged()

    override val showAlertIndicator: Flow<Boolean> = flowOf(false)

    private fun createApiStatusAlertItem(apiState: ApiState?): DydxAlertsProviderItemProtocol? {
        if (apiState?.status == null) {
            return null
        }

        val tapAction: (() -> Unit) = {
            // TODO   router.navigateTo()
        }
        when (apiState.status) {
            ApiStatus.INDEXER_DOWN -> {
                hadError = true
                return DydxAlertsView.Item(
                    title = localizer.localize("APP.V4.INDEXER_ALERT"),
                    message = localizer.localize("APP.V4.INDEXER_DOWN"),
                    icon = R.drawable.status_error,
                    tapAction = tapAction,
                    date = Date(),
                )
            }
            ApiStatus.INDEXER_HALTED -> {
                hadError = true
                return DydxAlertsView.Item(
                    title = localizer.localize("APP.V4.INDEXER_ALERT"),
                    message = localizer.localizeWithParams("APP.V4.INDEXER_HALTED", mapOf("HALTED_BLOCK" to (apiState.haltedBlock ?: 0).toString())),
                    icon = R.drawable.status_warning,
                    tapAction = tapAction,
                    date = Date(),
                )
            }
            ApiStatus.INDEXER_TRAILING -> {
                hadError = true
                return DydxAlertsView.Item(
                    title = localizer.localize("APP.V4.INDEXER_TRAILING"),
                    message = localizer.localizeWithParams("APP.V4.INDEXER_HALTED", mapOf("TRAILING_BLOCKS" to (apiState.trailingBlocks ?: 0).toString())),
                    icon = R.drawable.status_warning,
                    tapAction = tapAction,
                    date = Date(),
                )
            }
            ApiStatus.VALIDATOR_DOWN -> {
                hadError = true
                return DydxAlertsView.Item(
                    title = localizer.localize("APP.V4.VALIDATOR_ALERT"),
                    message = localizer.localize("APP.V4.VALIDATOR_DOWN"),
                    icon = R.drawable.status_error,
                    tapAction = tapAction,
                    date = Date(),
                )
            }
            ApiStatus.VALIDATOR_HALTED -> {
                hadError = true
                return DydxAlertsView.Item(
                    title = localizer.localize("APP.V4.VALIDATOR_ALERT"),
                    message = localizer.localizeWithParams("APP.V4.VALIDATOR_HALTED", mapOf("HALTED_BLOCK" to (apiState.haltedBlock ?: 0).toString())),
                    icon = R.drawable.status_warning,
                    tapAction = tapAction,
                    date = Date(),
                )
            }
            ApiStatus.NORMAL -> {
                return if (hadError) {
                    DydxAlertsView.Item(
                        title = localizer.localize("APP.V4.NETWORK_OPERATIONAL"),
                        message = localizer.localize("APP.V4.NETWORK_RECOVERED"),
                        icon = R.drawable.status_complete,
                        tapAction = tapAction,
                        date = Date(),
                    )
                } else {
                    null
                }
            }
            else -> {
                return null
            }
        }
    }
}
