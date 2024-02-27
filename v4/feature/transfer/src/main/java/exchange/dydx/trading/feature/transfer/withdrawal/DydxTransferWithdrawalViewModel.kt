package exchange.dydx.trading.feature.transfer.withdrawal

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
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.transfer.components.AddressInputBox
import exchange.dydx.trading.feature.transfer.components.ChainsComboBox
import exchange.dydx.trading.feature.transfer.components.TokensComboBox
import exchange.dydx.trading.feature.transfer.components.TransferAmountBox
import exchange.dydx.trading.feature.transfer.search.DydxTransferSearchParam
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
        abacusStateManager.state.transferInput
            .map { it?.withdrawalOptions?.chains?.toList() }
            .distinctUntilChanged()
            .onEach { chains ->
                selectedChainFlow.value = chains?.firstOrNull()
            }
            .launchIn(viewModelScope)

        abacusStateManager.state.transferInput
            .map { it?.withdrawalOptions?.assets?.toList() }
            .distinctUntilChanged()
            .onEach { tokens ->
                selectedTokenFlow.value = tokens?.firstOrNull()
            }
            .launchIn(viewModelScope)
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
        )
    }
}
