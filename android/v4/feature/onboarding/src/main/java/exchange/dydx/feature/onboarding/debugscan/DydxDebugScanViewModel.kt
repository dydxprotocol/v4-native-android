package exchange.dydx.feature.onboarding.debugscan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxCartera.DydxWalletSetup
import exchange.dydx.dydxCartera.v4.DydxV4WalletSetup
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.Logging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DydxDebugScanViewModel @Inject constructor(
    val router: DydxRouter,
    val cosmosV4Client: CosmosV4WebviewClientProtocol,
    val parser: ParserProtocol,
    val logger: Logging,
    val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    private val _state = MutableStateFlow(
        DydxDebugScanView.ViewState(),
    )
    val state: Flow<DydxDebugScanView.ViewState> = _state

    private var context: Context? = null
    private var walletSetup: DydxWalletSetup? = null

    fun updateContext(context: Context) {
        if (context != this.context) {
            this.context = context
            val walletSetup = DydxV4WalletSetup(context, cosmosV4Client, parser, logger)
            _state.update { state ->
                state.copy(
                    closeButtonHandler = {
                        walletSetup.stop()
                        router.navigateBack()
                    },
                )
            }

            walletSetup.debugLink.onEach { uri ->
                _state.update { state ->
                    state.copy(
                        uri = uri,
                    )
                }
            }.launchIn(viewModelScope)

            walletSetup.status.onEach { status ->
                if (status is DydxWalletSetup.Status.Signed) {
                    val result = status.setupResult
                    val ethereumAddress = result.ethereumAddress
                    val cosmosAddress = result.cosmosAddress
                    val mnemonic = result.dydxMnemonic
                    if (cosmosAddress != null && mnemonic != null) {
                        abacusStateManager.setV4(
                            ethereumAddress = ethereumAddress,
                            walletId = result.walletId,
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

                    walletSetup.stop()
                    router.navigateBack()
                }
            }.launchIn(viewModelScope)

            val chainId = abacusStateManager.environment?.ethereumChainId ?: "111555111"
            walletSetup.startDebugLink(chainId = chainId) { info, error ->
                if (error != null) {
                    _state.value = DydxDebugScanView.ViewState(
                        error = error.message,
                        closeButtonHandler = {
                            router.navigateBack()
                        },
                    )
                } else if (info?.chainId != null) {
                    val signTypedDataAction = abacusStateManager.environment?.walletConnection?.signTypedDataAction
                    val signTypedDataDomainName = abacusStateManager.environment?.walletConnection?.signTypedDataDomainName

                    if (signTypedDataAction != null && signTypedDataDomainName != null) {
                        walletSetup.start(
                            walletId = info.wallet?.id,
                            ethereumChainId = chainId.toInt(),
                            signTypedDataAction = signTypedDataAction,
                            signTypedDataDomainName = signTypedDataDomainName,
                        )
                    } else {
                        _state.value = DydxDebugScanView.ViewState(
                            error = "signTypedDataAction or signTypedDataDomainName is null",
                            closeButtonHandler = {
                                router.navigateBack()
                            },
                        )
                    }
                }
            }
            this.walletSetup = walletSetup
        }
    }
}
