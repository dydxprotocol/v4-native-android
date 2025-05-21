package exchange.dydx.trading.feature.transfer.noble

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.utils.AbacusStringUtils
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTransferNobleAddressViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val appConfig: AppConfig,
    private val platformInfo: PlatformInfo,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTransferNobleAddressView.ViewState?> =
        abacusStateManager.state.currentWallet
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        currentWallet: DydxWalletInstance?
    ): DydxTransferNobleAddressView.ViewState {
        val cosmosAddress = currentWallet?.cosmoAddress
        val nobleAddress = if (cosmosAddress != null) {
            AbacusStringUtils.toNobleAddress(cosmosAddress)
        } else {
            null
        }
        return DydxTransferNobleAddressView.ViewState(
            localizer = localizer,
            address = nobleAddress,
            backButtonAction = {
                router.navigateBack()
            },
            copyAction = {
                val clipboard = appConfig.appContext?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                if (nobleAddress == null || clipboard == null) {
                    return@ViewState
                }
                val clip = ClipData.newPlainText("text", nobleAddress)
                clipboard.setPrimaryClip(clip)
                platformInfo.show(
                    message = localizer.localize("APP.V4.NOBLE_ADDRESS_COPIED"),
                )
            },
        )
    }
}
