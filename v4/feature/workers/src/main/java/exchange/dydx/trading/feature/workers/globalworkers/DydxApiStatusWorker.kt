package exchange.dydx.trading.feature.workers.globalworkers

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.ApiState
import exchange.dydx.abacus.state.manager.ApiStatus
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ActivityRetainedScoped
class DydxApiStatusWorker @Inject constructor(
    @CoroutineScopes.App private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val localizer: LocalizerProtocol,
    private val toaster: PlatformInfo,
) : WorkerProtocol {
    private var lastState: ApiState? = null

    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            abacusStateManager.state.apiState
                .onEach { apiState ->
                    updateApiStatus(apiState)
                }
                .launchIn(scope)
        }
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }

    private fun updateApiStatus(apiState: ApiState?) {
        if (lastState == apiState || lastState?.status == apiState?.status) {
            return
        }

        val status = apiState?.status ?: return
        val buttonTitle = localizer.localize("APP.GENERAL.DISMISS")
        val buttonAction = {
        }
        when (status) {
            ApiStatus.INDEXER_DOWN -> {
                toaster.show(
                    message = localizer.localize("APP.V4.INDEXER_DOWN"),
                    buttonTitle = buttonTitle,
                    type = PlatformInfoViewModel.Type.Error,
                    buttonAction = buttonAction,
                )
            }
            ApiStatus.INDEXER_HALTED -> {
                toaster.show(
                    message = localizer.localizeWithParams("APP.V4.INDEXER_HALTED", mapOf("HALTED_BLOCK" to (apiState.haltedBlock ?: 0).toString())) ?: "",
                    buttonTitle = buttonTitle,
                    type = PlatformInfoViewModel.Type.Warning,
                    buttonAction = buttonAction,
                )
            }
            ApiStatus.INDEXER_TRAILING -> {
                toaster.show(
                    message = localizer.localizeWithParams("APP.V4.INDEXER_TRAILING", mapOf("TRAILING_BLOCKS" to (apiState.trailingBlocks ?: 0).toString())) ?: "",
                    buttonTitle = buttonTitle,
                    type = PlatformInfoViewModel.Type.Warning,
                    buttonAction = buttonAction,
                )
            }
            ApiStatus.VALIDATOR_DOWN -> {
                toaster.show(
                    message = localizer.localize("APP.V4.VALIDATOR_DOWN"),
                    buttonTitle = buttonTitle,
                    type = PlatformInfoViewModel.Type.Error,
                    buttonAction = buttonAction,
                )
            }
            ApiStatus.VALIDATOR_HALTED -> {
                toaster.show(
                    message = localizer.localizeWithParams("APP.V4.VALIDATOR_HALTED", mapOf("HALTED_BLOCK" to (apiState.haltedBlock ?: 0).toString())) ?: "",
                    buttonTitle = buttonTitle,
                    type = PlatformInfoViewModel.Type.Warning,
                    buttonAction = buttonAction,
                )
            }
            ApiStatus.NORMAL -> {
                if (lastState != null) {
                    toaster.show(
                        message = localizer.localize("APP.V4.NETWORK_RECOVERED"),
                    )
                }
            }

            else -> {}
        }

        lastState = apiState
    }
}
