package exchange.dydx.trading.feature.profile.userwallets

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletState
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.KeyExportType
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.ProfileRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxUserWalletsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxUserWalletsView.ViewState?> =
        combine(
            abacusStateManager.state.walletState,
            abacusStateManager.state.currentWallet,
        ) { walletState, currentWallet ->
            createViewState(walletState, currentWallet)
        }
            .distinctUntilChanged()

    private fun createViewState(
        walletState: DydxWalletState?,
        currentWallet: DydxWalletInstance?,
    ): DydxUserWalletsView.ViewState {
        val folder = abacusStateManager.environment?.walletConnection?.images
        return DydxUserWalletsView.ViewState(
            localizer = localizer,
            wallets = walletState?.wallets?.map {
                DydxUserWalletItemView.ViewState(
                    localizer = localizer,
                    iconUrl = if (folder != null) it.imageUrl(folder) else null,
                    address = it.ethereumAddress ?: it.cosmoAddress,
                    isSelected = it.ethereumAddress == currentWallet?.ethereumAddress,
                    explorerLinkAction = {
                        val url = "https://etherscan.io/address/${it.ethereumAddress}"
                        router.navigateTo(url)
                    },
                    exportAction = {
                        router.navigateTo(
                            route = ProfileRoutes.key_export + "/${KeyExportType.dydx}",
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    },
                )
            } ?: emptyList(),
            closeAction = {
                router.navigateBack()
            },
            addWalletAction = {
                router.navigateBack()
                router.navigateTo(
                    route = OnboardingRoutes.wallet_list,
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            onWalletSelected = { index ->
                val wallet = walletState?.wallets?.getOrNull(index)
                val cosmosAddress = wallet?.cosmoAddress
                val mnemonic = wallet?.mnemonic
                if (wallet != null && cosmosAddress != null && mnemonic != null) {
                    abacusStateManager.setV4(
                        ethereumAddress = wallet.ethereumAddress,
                        walletId = wallet.walletId,
                        cosmosAddress = cosmosAddress,
                        dydxMnemonic = mnemonic,
                        isNew = true,
                        svmAddress = null,
                        avalancheAddress = null,
                        sourceWalletMnemonic = null,
                        loginMethod = null,
                        userEmail = null,
                    )
                }
            },
        )
    }
}
