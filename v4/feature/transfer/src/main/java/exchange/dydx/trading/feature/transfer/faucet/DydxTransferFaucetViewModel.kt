package exchange.dydx.trading.feature.transfer.faucet

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.Toast
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxTransferFaucetViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val toaster: PlatformInfo,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTransferFaucetView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxTransferFaucetView.ViewState {
        return DydxTransferFaucetView.ViewState(
            localizer = localizer,
            ctaButtonAction = {
                abacusStateManager.faucet(100) {
                    when (it) {
                        is AbacusStateManagerProtocol.SubmissionStatus.Success -> {
                            toaster.show(
                                title = "Faucet Request Submitted",
                                message = "Your portfolio balance will be updated after a short while.",
                            )
                        }
                        is AbacusStateManagerProtocol.SubmissionStatus.Failed -> {
                            toaster.show(
                                title = "Faucet Request Failed",
                                message = it.error?.message ?: "Unknown error",
                                type = Toast.Type.Success,
                            )
                        }
                    }
                }
            },
        )
    }
}
