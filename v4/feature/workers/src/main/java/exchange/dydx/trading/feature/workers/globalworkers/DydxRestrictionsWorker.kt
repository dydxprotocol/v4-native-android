package exchange.dydx.trading.feature.workers.globalworkers

import androidx.compose.material.SnackbarDuration
import exchange.dydx.abacus.output.ComplianceStatus.BLOCKED
import exchange.dydx.abacus.output.ComplianceStatus.CLOSE_ONLY
import exchange.dydx.abacus.output.ComplianceStatus.COMPLIANT
import exchange.dydx.abacus.output.ComplianceStatus.FIRST_STRIKE
import exchange.dydx.abacus.output.ComplianceStatus.FIRST_STRIKE_CLOSE_ONLY
import exchange.dydx.abacus.output.ComplianceStatus.UNKNOWN
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.trading.feature.shared.DydxScreenResult
import exchange.dydx.utilities.utils.WorkerProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DydxRestrictionsWorker(
    override val scope: CoroutineScope,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val localizer: LocalizerProtocol,
    private val platformInfo: PlatformInfo,
) : WorkerProtocol {
    override var isStarted = false

    override fun start() {
        if (!isStarted) {
            isStarted = true

            abacusStateManager.state.restriction
                .onEach { restriction ->
                    val screenResult = DydxScreenResult.from(restriction)
                    screenResult.showRestrictionAlert(platformInfo, localizer, abacusStateManager)
                }
                .launchIn(scope)

            abacusStateManager.state.compliance
                .onEach { compliance ->
                    val (title, body) = when (compliance.status) {
                        UNKNOWN, COMPLIANT -> return@onEach
                        FIRST_STRIKE,
                        FIRST_STRIKE_CLOSE_ONLY,
                        CLOSE_ONLY -> {
                            val params = mapOf("DATE" to compliance.expiresAt.orEmpty(), "EMAIL" to abacusStateManager.environment?.links?.complianceSupportEmail.orEmpty())
                            localizer.localize("APP.COMPLIANCE.CLOSE_ONLY_TITLE") to localizer.localizeWithParams("APP.COMPLIANCE.CLOSE_ONLY_BODY", params)
                        }
                        BLOCKED -> {
                            val params = mapOf("EMAIL" to abacusStateManager.environment?.links?.complianceSupportEmail.orEmpty())
                            localizer.localize("APP.COMPLIANCE.PERMANENTLY_BLOCKED_TITLE") to localizer.localizeWithParams("APP.COMPLIANCE.PERMANENTLY_BLOCKED_BODY", params)
                        }
                    }

                    platformInfo.show(
                        title = title,
                        message = body.orEmpty(),
                        type = PlatformInfo.InfoType.Error,
                        duration = SnackbarDuration.Indefinite,
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
