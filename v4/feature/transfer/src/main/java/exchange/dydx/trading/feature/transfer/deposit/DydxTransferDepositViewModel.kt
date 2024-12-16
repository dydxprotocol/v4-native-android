package exchange.dydx.trading.feature.transfer.deposit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.transfer.components.ChainsComboBox
import exchange.dydx.trading.feature.transfer.components.TokensComboBox
import exchange.dydx.trading.feature.transfer.components.TransferAmountBox
import exchange.dydx.trading.feature.transfer.search.DydxTransferSearchParam
import exchange.dydx.utilities.utils.Logging
import exchange.dydx.web3.EthereumInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class DydxTransferDepositViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val router: DydxRouter,
    private val paramFlow: MutableStateFlow<DydxTransferSearchParam?>,
    private val logger: Logging,
) : ViewModel(), DydxViewModel {

    private val TAG = "DydxTransferDepositViewModel"

    private var ethereumInteractor: EthereumInteractor? = null

    private val selectedChainFlow: MutableStateFlow<SelectionOption?> = MutableStateFlow(null)
    private val selectedTokenFlow: MutableStateFlow<SelectionOption?> = MutableStateFlow(null)
    private val tokenAmountFLow: MutableStateFlow<Double?> = MutableStateFlow(null)

    val state: Flow<DydxTransferDepositView.ViewState?> =
        combine(
            abacusStateManager.state.transferInput,
            selectedChainFlow,
            selectedTokenFlow,
            tokenAmountFLow,
            abacusStateManager.state.currentWallet.map { it?.ethereumAddress }.distinctUntilChanged(),
        ) { transferInput, selectedChain, selectedToken, tokenMaxAmount, ethereumAddress ->
            createViewState(transferInput, selectedChain, selectedToken, tokenMaxAmount, ethereumAddress.isNullOrEmpty())
        }
            .distinctUntilChanged()

    init {
        combine(
            abacusStateManager.state.transferInput
                .map { it?.depositOptions?.chains?.toList() }
                .distinctUntilChanged(),
            abacusStateManager.state.transferInput.map { it?.chain }.distinctUntilChanged(),
        ) { chains, selected ->
            chains?.firstOrNull { it.type == selected } ?: chains?.firstOrNull()
        }
            .onEach { chain ->
                selectedChainFlow.value = chain
            }
            .launchIn(viewModelScope)

        combine(
            abacusStateManager.state.transferInput
                .map { it?.depositOptions?.assets?.toList() }
                .distinctUntilChanged(),
            abacusStateManager.state.transferInput.map { it?.token }.distinctUntilChanged(),
        ) { tokens, selected ->
            tokens?.firstOrNull { it.type == selected } ?: tokens?.firstOrNull()
        }
            .onEach { token ->
                selectedTokenFlow.value = token
            }
            .launchIn(viewModelScope)

        combine(
            abacusStateManager.state.transferInput.mapNotNull { it?.resources }.distinctUntilChanged(),
            abacusStateManager.state.transferInput.mapNotNull { it?.chain }.distinctUntilChanged(),
            abacusStateManager.state.transferInput.mapNotNull { it?.token }.distinctUntilChanged(),
            abacusStateManager.state.currentWallet.mapNotNull { it?.ethereumAddress }.distinctUntilChanged(),
        ) { resources, chain, token, ethereumAddress ->
            val chainRpc = resources.chainResources?.get(chain)?.rpc ?: return@combine null
            val tokenResource = resources.tokenResources?.get(token) ?: return@combine null
            val tokenDecimals = tokenResource.decimals ?: return@combine null
            if (ethereumAddress.isNullOrEmpty()) {
                return@combine null
            }
            fetchTokenAmount(chainRpc, token, tokenDecimals, ethereumAddress)
        }
            .launchIn(viewModelScope)
    }

    private fun fetchTokenAmount(
        chainRpc: String,
        tokenAddress: String,
        tokenDecimals: Int,
        ethereumAddress: String,
    ) {
        ethereumInteractor = EthereumInteractor(chainRpc)
        if (tokenAddress == "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE") {
            ethereumInteractor?.ethGetBalance(ethereumAddress) { error, balance ->
                if (error == null && balance != null) {
                    val tokenAmount = balance.toDouble() / Math.pow(10.0, tokenDecimals.toDouble())
                    tokenAmountFLow.value = tokenAmount
                } else {
                    logger.e(TAG, "Failed to fetch token amount (ethGetBalance) $error")
                }
            }
        } else {
            ethereumInteractor?.erc20TokenGetBalance(
                accountAddress = ethereumAddress,
                tokenAddress = tokenAddress,
            ) { error, balance ->
                if (error == null && balance != null) {
                    val tokenAmount = balance.toDouble() / Math.pow(10.0, tokenDecimals.toDouble())
                    tokenAmountFLow.value = tokenAmount
                } else {
                    logger.e(TAG, "Failed to fetch token amount (erc20TokenGetBalance) $error")
                }
            }
        }
    }

    private fun createViewState(
        transferInput: TransferInput?,
        chain: SelectionOption?,
        token: SelectionOption?,
        tokenMaxAmount: Double?,
        showConnectWallet: Boolean,
    ): DydxTransferDepositView.ViewState {
        val tokenAddress = token?.type
        val tokenSymbol = if (tokenAddress != null) {
            transferInput?.resources?.tokenResources?.get(tokenAddress)?.symbol
        } else {
            null
        }
        return DydxTransferDepositView.ViewState(
            localizer = localizer,
            chainsComboBox = if (chain != null) {
                ChainsComboBox.ViewState(
                    localizer = localizer,
                    text = chain.localizedString(localizer),
                    label = localizer.localize("APP.GENERAL.SOURCE"),
                    icon = chain.iconUrl,
                    onTapAction = {
                        paramFlow.value = DydxTransferSearchParam(
                            options = transferInput?.depositOptions?.chains?.toList(),
                            selected = chain,
                            resources = transferInput?.resources,
                            selectedCallback = { selected ->
                                if (selected != selectedChainFlow.value) {
                                    selectedChainFlow.value = selected
                                    abacusStateManager.transfer(
                                        selected.type,
                                        TransferInputField.chain,
                                    )
                                }
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
                            options = transferInput?.depositOptions?.assets?.toList(),
                            selected = token,
                            resources = transferInput?.resources,
                            selectedCallback = { selected ->
                                if (selected != selectedTokenFlow.value) {
                                    selectedTokenFlow.value = selected
                                    abacusStateManager.transfer(
                                        selected.type,
                                        TransferInputField.token,
                                    )
                                }
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
                tokenText = if (tokenSymbol != null) {
                    TokenTextView.ViewState(
                        symbol = tokenSymbol,
                    )
                } else {
                    null
                },
                value = transferInput?.size?.size,
                maxAmount = tokenMaxAmount ?: 0.0,
                stepSize = 3,
                onEditAction = { amount ->
                    val size = min(tokenMaxAmount ?: 0.0, parser.asDouble(amount) ?: 0.0)
                    val rawString = formatter.raw(size) ?: "0"
                    abacusStateManager.transfer(
                        rawString,
                        TransferInputField.size,
                    )
                },
            ),
            showConnectWallet = showConnectWallet,
            connectWalletAction = {
                router.navigateTo(
                    route = OnboardingRoutes.wallet_list + "?mobileOnly=true",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
        )
    }
}
