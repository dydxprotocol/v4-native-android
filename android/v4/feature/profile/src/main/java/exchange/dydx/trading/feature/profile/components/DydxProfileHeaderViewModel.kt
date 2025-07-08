package exchange.dydx.trading.feature.profile.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletState
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxProfileHeaderViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val appConfig: AppConfig,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val toaster: PlatformInfo,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxProfileHeaderView.ViewState?> = abacusStateManager.state.walletState
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(walletState: DydxWalletState?): DydxProfileHeaderView.ViewState {
        return DydxProfileHeaderView.ViewState(
            localizer = localizer,
            dydxChainLogoUrl = abacusStateManager.environment?.chainLogo,
            dydxAddress = walletState?.currentWallet?.cosmoAddress,
            sourceAddress = walletState?.currentWallet?.ethereumAddress,
            copyAddressAction = {
                val clipboard = appConfig.appContext?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                if (walletState?.currentWallet?.cosmoAddress == null || clipboard == null) {
                    return@ViewState
                }
                val clip = ClipData.newPlainText("text", walletState.currentWallet?.cosmoAddress)
                clipboard.setPrimaryClip(clip)
                toaster.show(
                    message = localizer.localize("APP.V4.DYDX_ADDRESS_COPIED"),
                )
            },
            blockExplorerAction = {
                if (abacusStateManager.environment?.links?.mintscanBase == null && walletState?.currentWallet?.cosmoAddress == null) {
                    return@ViewState
                }
                val url = abacusStateManager.environment?.links?.mintscanBase + "/address/" + walletState?.currentWallet?.cosmoAddress
                router.navigateTo(url)
            },
        )
    }
}
