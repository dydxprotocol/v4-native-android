package exchange.dydx.trading.feature.transfer.deposit.qrcode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.localizeWithParams
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.depositaddresses.DepositAddresses
import exchange.dydx.dydxstatemanager.clientState.depositaddresses.DydxDepositAddressesStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.RemoteFlags
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
    private val depositAddressesStateManager: DydxDepositAddressesStateManagerProtocol,
    private val router: DydxRouter,
    savedStateHandle: SavedStateHandle,
    private val remoteFlags: RemoteFlags,
) : ViewModel(), DydxViewModel {
    private val chain: String? = savedStateHandle["chain"]

    private val copied = MutableStateFlow(false)

    val state: Flow<DydxTurnkeyQRCodeView.ViewState?> =
        combine(
            depositAddressesStateManager.state,
            copied,
        ) { depositAddresses, copied ->
            createViewState(depositAddresses, copied)
        }
            .distinctUntilChanged()

    private fun createViewState(
        addresses: DepositAddresses?,
        copied: Boolean,
    ): DydxTurnkeyQRCodeView.ViewState? {
        if (addresses == null || chain.isNullOrEmpty()) {
            return null
        }
        val chain = TransferChain.fromString(chain) ?: return null
        val address = when (chain) {
            TransferChain.Solana -> addresses.svmAddress
            TransferChain.Ethereum -> addresses.evmAddress
            TransferChain.Arbitrum -> addresses.evmAddress
            TransferChain.Base -> addresses.evmAddress
            TransferChain.Optimism -> addresses.evmAddress
            TransferChain.Avalanche -> addresses.avalancheAddress
            else -> null
        }
        val minSlowVal = if (chain == TransferChain.Ethereum) {
            remoteFlags.getConfigValue("turnkey_deposit", "eth_min_slow", "-")
        } else {
            remoteFlags.getConfigValue("turnkey_deposit", "default_min_slow", "-")
        }
        val minFastVal = if (chain == TransferChain.Ethereum) {
            remoteFlags.getConfigValue("turnkey_deposit", "eth_min_fast", "-")
        } else {
            remoteFlags.getConfigValue("turnkey_deposit", "default_min_fast", "-")
        }
        val maxVal = if (chain == TransferChain.Ethereum) {
            remoteFlags.getConfigValue("turnkey_deposit", "eth_max", "-")
        } else {
            remoteFlags.getConfigValue("turnkey_deposit", "default_max", "-")
        }
        return DydxTurnkeyQRCodeView.ViewState(
            localizer = localizer,
            backAction = {
                router.navigateBack()
            },
            address = address,
            chainIconUrl = chain.chainLogoUrl(abacusStateManager.deploymentUri),
            subtitle = localizer.localizeWithParams(
                path = "APP.TURNKEY_ONBOARD.DEPOSIT_NETWORK_WARNING",
                params = mapOf(
                    "ASSETS" to chain.supportedDepositTokenString,
                    "NETWORK" to chain.name,
                    "MIN_DEPOSIT" to minSlowVal,
                    "MIN_INSTANT_DEPOSIT" to minFastVal,
                    "MAX_DEPOSIT" to maxVal,
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
