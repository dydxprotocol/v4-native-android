package exchange.dydx.trading.feature.profile.keyexport

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletState
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxKeyExportViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    @ApplicationContext private val context: Context,
    val platformInfo: PlatformInfo,
) : ViewModel(), DydxViewModel {

    private val exportStateFlow = MutableStateFlow(DydxKeyExportView.State.Warning)

    val state: Flow<DydxKeyExportView.ViewState?> =
        combine(
            abacusStateManager.state.walletState,
            exportStateFlow,
        ) { walletState, exportState ->
            createViewState(walletState, exportState)
        }
            .distinctUntilChanged()

    private fun createViewState(
        walletState: DydxWalletState?,
        exportState: DydxKeyExportView.State,
    ): DydxKeyExportView.ViewState {
        return DydxKeyExportView.ViewState(
            localizer = localizer,
            closeAction = { router.navigateBack() },
            phrase = walletState?.currentWallet?.mnemonic ?: "",
            exportState = exportState,
            stateAction = {
                when (it) {
                    DydxKeyExportView.State.Warning -> {
                        exportStateFlow.value = DydxKeyExportView.State.NotRevealed
                    }
                    DydxKeyExportView.State.NotRevealed -> {
                        exportStateFlow.value = DydxKeyExportView.State.Revealed
                    }
                    else -> {
                        DydxKeyExportView.State.Warning
                    }
                }
            },
            copyAction = {
                platformInfo.show(
                    message = localizer.localize("APP.V4.DYDX_MNEMONIC_COPIED"),
                )
            },
        )
    }
}
