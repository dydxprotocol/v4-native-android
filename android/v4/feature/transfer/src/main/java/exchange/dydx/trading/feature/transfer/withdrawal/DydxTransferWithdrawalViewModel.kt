package exchange.dydx.trading.feature.transfer.withdrawal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.machine.TransferInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxBoolFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.transfer.components.AddressInputBox
import exchange.dydx.trading.feature.transfer.components.ChainsComboBox
import exchange.dydx.trading.feature.transfer.components.TokensComboBox
import exchange.dydx.trading.feature.transfer.components.TransferAmountBox
import exchange.dydx.trading.feature.transfer.search.DydxTransferSearchParam
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class DydxTransferWithdrawalViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val router: DydxRouter,
    private val paramFlow: MutableStateFlow<DydxTransferSearchParam?>,
    private val featureFlags: DydxFeatureFlags,
) : ViewModel(), DydxViewModel {
    private val selectedChainFlow: MutableStateFlow<SelectionOption?> = MutableStateFlow(null)
    private val selectedTokenFlow: MutableStateFlow<SelectionOption?> = MutableStateFlow(null)

    val state: Flow<DydxTransferWithdrawalView.ViewState?> =
        combine(
            abacusStateManager.state.transferInput,
            abacusStateManager.state.selectedSubaccount
                .map { it?.freeCollateral?.current },
            selectedChainFlow,
            selectedTokenFlow,
        ) { transferInput, freeCollateral, selectedChain, selectedToken ->
            createViewState(transferInput, freeCollateral, selectedChain, selectedToken)
        }
            .distinctUntilChanged()

    init {
        combine(
            abacusStateManager.state.transferInput
                .map { it?.withdrawalOptions?.chains?.toList() }
                .distinctUntilChanged(),
            abacusStateManager.state.transferInput.map { it?.chain }.distinctUntilChanged(),
        ) { chains, selected ->
            chains?.firstOrNull { it.type == selected } ?: chains?.firstOrNull()
        }
            .onEach { selectedChainFlow.value = it }
            .launchIn(viewModelScope)

        combine(
            abacusStateManager.state.transferInput
                .map { it?.withdrawalOptions?.assets?.toList() }
                .distinctUntilChanged(),
            abacusStateManager.state.transferInput.map { it?.token }.distinctUntilChanged(),
        ) { tokens, selected ->
            tokens?.firstOrNull { it.type == selected } ?: tokens?.firstOrNull()
        }
            .onEach { selectedTokenFlow.value = it }
            .launchIn(viewModelScope)

        if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_turnkey_android)) {
            abacusStateManager.startWithdrawal()
            abacusStateManager.state.currentWallet
                .take(1)
                .onStart { delay(100) }
                .onEach { wallet ->
                    if (wallet?.walletId != "turnkey") {
                        abacusStateManager.transfer(
                            input = wallet?.ethereumAddress,
                            type = TransferInputField.address,
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun createViewState(
        transferInput: TransferInput?,
        freeCollateral: Double?,
        chain: SelectionOption?,
        token: SelectionOption?,
    ): DydxTransferWithdrawalView.ViewState {
        val tokenAddress = token?.type
        val tokenSymbol = if (tokenAddress != null) {
            transferInput?.resources?.tokenResources?.get(tokenAddress)?.symbol
        } else {
            null
        }
        if (transferInput?.token != tokenAddress) {
            abacusStateManager.transfer(input = tokenAddress, type = TransferInputField.token)
        }

        return DydxTransferWithdrawalView.ViewState(
            localizer = localizer,
            chainsComboBox = if (chain != null) {
                ChainsComboBox.ViewState(
                    localizer = localizer,
                    text = chain.localizedString(localizer),
                    label = localizer.localize("APP.GENERAL.NETWORK"),
                    icon = chain.iconUrl,
                    onTapAction = {
                        paramFlow.value = DydxTransferSearchParam(
                            options = transferInput?.withdrawalOptions?.chains?.toList(),
                            selected = chain,
                            resources = transferInput?.resources,
                            selectedCallback = { selected ->
                                selectedChainFlow.value = selected
                                abacusStateManager.transfer(
                                    selected.type,
                                    TransferInputField.chain,
                                )
                            },
                        )
                        router.navigateTo(
                            route = TransferRoutes.transfer_search,
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    },
                )
            } else {
                null
            },
            tokensComboBox = if (token != null) {
                TokensComboBox.ViewState(
                    localizer = localizer,
                    text = token.localizedString(localizer),
                    label = localizer.localize("APP.GENERAL.ASSET"),
                    icon = token.iconUrl,
                    tokenText = if (tokenSymbol != null) {
                        TokenTextView.ViewState(
                            symbol = tokenSymbol,
                        )
                    } else {
                        null
                    },
                    onTapAction = {
                        paramFlow.value = DydxTransferSearchParam(
                            options = transferInput?.withdrawalOptions?.assets?.toList(),
                            selected = token,
                            resources = transferInput?.resources,
                            selectedCallback = { selected ->
                                selectedTokenFlow.value = selected
                                abacusStateManager.transfer(
                                    selected.type,
                                    TransferInputField.token,
                                )
                            },
                        )
                        router.navigateTo(
                            route = TransferRoutes.transfer_search,
                            presentation = DydxRouter.Presentation.Modal,
                        )
                    },
                )
            } else {
                null
            },
            transferAmount = TransferAmountBox.ViewState(
                localizer = localizer,
                formatter = formatter,
                parser = parser,
                value = transferInput?.size?.usdcSize,
                tokenText = if (tokenSymbol != null) {
                    TokenTextView.ViewState(
                        symbol = tokenSymbol,
                    )
                } else {
                    null
                },
                maxAmount = freeCollateral ?: 0.0,
                stepSize = 2,
                onEditAction = { amount ->
                    var raw: Double = parser.asDouble(amount) ?: 0.0
                    raw = min(raw, freeCollateral ?: 0.0)
                    val rawString = if (raw > 0) formatter.raw(raw) else null
                    abacusStateManager.transfer(
                        input = rawString,
                        type = TransferInputField.usdcSize,
                    )
                },
            ),
            addressInput = AddressInputBox.ViewState(
                localizer = localizer,
                formatter = formatter,
                parser = parser,
                value = transferInput?.address,
                placeholder = "0x00...0000",
            ),
            closeAction = if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_turnkey_android)) {
                {
                    router.navigateBack()
                }
            } else {
                null
            },
        )
    }
}
