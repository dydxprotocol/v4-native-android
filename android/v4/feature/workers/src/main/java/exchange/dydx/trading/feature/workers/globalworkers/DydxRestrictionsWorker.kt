package exchange.dydx.trading.feature.workers.globalworkers

import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.output.ComplianceStatus.BLOCKED
import exchange.dydx.abacus.output.ComplianceStatus.CLOSE_ONLY
import exchange.dydx.abacus.output.ComplianceStatus.COMPLIANT
import exchange.dydx.abacus.output.ComplianceStatus.FIRST_STRIKE
import exchange.dydx.abacus.output.ComplianceStatus.FIRST_STRIKE_CLOSE_ONLY
import exchange.dydx.abacus.output.ComplianceStatus.UNKNOWN
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.feature.shared.DydxScreenResult
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ActivityRetainedScoped
class DydxRestrictionsWorker @Inject constructor(
    @CoroutineScopes.App private val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val localizer: LocalizerProtocol,
    private val toaster: PlatformInfo,
) : WorkerProtocol {
    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            abacusStateManager.state.restriction
                .onEach { restriction ->
                    val screenResult = DydxScreenResult.from(restriction)
                    screenResult.showRestrictionAlert(toaster, localizer, abacusStateManager)
                }
                .launchIn(scope)

            abacusStateManager.state.compliance
                .onEach { compliance ->
                    val (title, body) = when (compliance.status) {
                        UNKNOWN, COMPLIANT, FIRST_STRIKE -> return@onEach
                        FIRST_STRIKE_CLOSE_ONLY,
                        CLOSE_ONLY -> {
                            val params = mapOf("DATE" to compliance.expiresAt.orEmpty(), "HELP_LINK" to abacusStateManager.environment?.links?.help.orEmpty())
                            localizer.localize("APP.COMPLIANCE.CLOSE_ONLY_TITLE") to localizer.localizeWithParams("APP.COMPLIANCE.CLOSE_ONLY_BODY", params)
                        }
                        BLOCKED -> {
                            val params = mapOf("HELP_LINK" to abacusStateManager.environment?.links?.help.orEmpty())
                            localizer.localize("APP.COMPLIANCE.PERMANENTLY_BLOCKED_TITLE") to localizer.localizeWithParams("APP.COMPLIANCE.PERMANENTLY_BLOCKED_BODY", params)
                        }
                    }

                    toaster.show(
                        title = title,
                        message = body.orEmpty(),
                        type = PlatformInfoViewModel.Type.Error,
                        duration = PlatformInfoViewModel.Duration.Indefinite,
                        cancellable = compliance.status != BLOCKED,
                    )
                    abacusStateManager.replaceCurrentWallet()
                }
                .launchIn(scope)
        }
    }

    override fun stop() {
        if (isStarted) {
            isStarted = false
        }
    }
}
