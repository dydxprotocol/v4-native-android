package exchange.dydx.trading.feature.transfer.deposit.qrcode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.localizeWithParams
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.TransferChain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTurnkeyQRCodeViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DydxViewModel {
    private val chain: String? = savedStateHandle["chain"]

    private val copied = MutableStateFlow(false)

    val state: Flow<DydxTurnkeyQRCodeView.ViewState?> =
        combine(
            abacusStateManager.state.currentWallet,
            copied,
        ) { currentWallet, copied ->
            createViewState(currentWallet, copied)
        }
            .distinctUntilChanged()

    private fun createViewState(
        wallet: DydxWalletInstance?,
        copied: Boolean,
    ): DydxTurnkeyQRCodeView.ViewState? {
        if (wallet == null || chain.isNullOrEmpty()) {
            return null
        }
        val chain = TransferChain.fromString(chain) ?: return null
        val address = when (chain) {
            TransferChain.Solana -> wallet.svmAddress
            TransferChain.Ethereum -> wallet.ethereumAddress
            TransferChain.Arbitrum -> wallet.ethereumAddress
            TransferChain.Base -> wallet.ethereumAddress
            TransferChain.Optimism -> wallet.ethereumAddress
            TransferChain.Avalanche -> wallet.avalancheAddress
            else -> null
        }
        return DydxTurnkeyQRCodeView.ViewState(
            localizer = localizer,
            backAction = {
                router.navigateBack()
            },
            address = address,
            chainIconUrl = chain.chainLogoUrl(abacusStateManager.deploymentUri),
            subtitle = localizer.localizeWithParams(
                path = "APP.DEPOSIT_MODAL.TURNKEY_DEPOSIT_SUBTITLE",
                params = mapOf(
                    "NETWORK" to chain.name,
                ),
            ),
            copied = copied,
            footer = chain.depositWarningString(localizer = localizer),
            onCopyAction = {
                this.copied.value = true
            },
        )
    }
}
