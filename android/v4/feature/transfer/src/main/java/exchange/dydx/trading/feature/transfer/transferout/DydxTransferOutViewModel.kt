package exchange.dydx.trading.feature.transfer.transferout

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
import exchange.dydx.dydxstatemanager.nativeTokenDenom
import exchange.dydx.dydxstatemanager.nativeTokenKey
import exchange.dydx.dydxstatemanager.usdcTokenKey
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
import exchange.dydx.trading.feature.transfer.components.TransferMemoBox
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
class DydxTransferOutViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val parser: ParserProtocol,
    private val router: DydxRouter,
    private val paramFlow: MutableStateFlow<DydxTransferSearchParam?>,
    private val featureFlags: DydxFeatureFlags,
) : ViewModel(), DydxViewModel {
    private val selectedTokenFlow: MutableStateFlow<SelectionOption?> = MutableStateFlow(null)

    val state: Flow<DydxTransferOutView.ViewState?> =
        combine(
            abacusStateManager.state.transferInput,
            abacusStateManager.state.selectedSubaccount
                .map { it?.freeCollateral?.current },
            abacusStateManager.state.accountBalance(abacusStateManager.nativeTokenDenom),
            selectedTokenFlow,
        ) { transferInput, freeCollateral, nativeTokenAmount, selectedToken ->
            createViewState(transferInput, freeCollateral, nativeTokenAmount, selectedToken)
        }
            .distinctUntilChanged()

    init {
        abacusStateManager.state.transferInput
            .map { input ->
                input?.transferOutOptions?.assets?.run {
                    firstOrNull { it.type == input.token } ?: firstOrNull()
                }
            }
            .distinctUntilChanged()
            .onEach {
                selectedTokenFlow.value = it
            }
            .launchIn(viewModelScope)

        if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_turnkey_android)) {
            abacusStateManager.transfer(input = null, type = TransferInputField.address)
            abacusStateManager.startTransferOut()
        }
    }

    private fun createViewState(
        transferInput: TransferInput?,
        freeCollateral: Double?,
        nativeTokenAmount: Double?,
        token: SelectionOption?,
    ): DydxTransferOutView.ViewState {
        val tokenSymbol = token?.localizedString(localizer)
        return DydxTransferOutView.ViewState(
            localizer = localizer,
            chainsComboBox = ChainsComboBox.ViewState(
                localizer = localizer,
                text = abacusStateManager.environment?.chainName,
                label = localizer.localize("APP.GENERAL.NETWORK"),
                icon = abacusStateManager.environment?.chainLogo,
                onTapAction = null,
            ),
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
                            options = transferInput?.transferOutOptions?.assets?.toList(),
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
                value = when (token?.type) {
                    abacusStateManager.usdcTokenKey -> transferInput?.size?.usdcSize
                    abacusStateManager.nativeTokenKey -> transferInput?.size?.size
                    else -> null
                },
                tokenText = if (tokenSymbol != null) {
                    TokenTextView.ViewState(
                        symbol = tokenSymbol,
                    )
                } else {
                    null
                },
                maxAmount = when (token?.type) {
                    abacusStateManager.usdcTokenKey -> freeCollateral ?: 0.0
                    abacusStateManager.nativeTokenKey -> nativeTokenAmount ?: 0.0
                    else -> null
                },
                stepSize = when (token?.type) {
                    abacusStateManager.usdcTokenKey -> 2
                    abacusStateManager.nativeTokenKey -> 3
                    else -> 0
                },
                onEditAction = { amount ->
                    var raw: Double = parser.asDouble(amount) ?: 0.0
                    if (token?.type == abacusStateManager.usdcTokenKey) {
                        raw = min(raw, freeCollateral ?: 0.0)
                        val rawString = if (raw > 0) formatter.raw(raw) else null
                        abacusStateManager.transfer(
                            rawString,
                            TransferInputField.usdcSize,
                        )
                    } else if (token?.type == abacusStateManager.nativeTokenKey) {
                        raw = min(raw, nativeTokenAmount ?: 0.0)
                        val rawString = if (raw > 0) formatter.raw(raw) else null
                        abacusStateManager.transfer(
                            rawString,
                            TransferInputField.size,
                        )
                    }
                },
            ),
            transferMemo = TransferMemoBox.ViewState(
                localizer = localizer,
                value = transferInput?.memo,
                showCexWarning = transferInput?.token != abacusStateManager.usdcTokenKey && transferInput?.memo.isNullOrEmpty(),
                onEditAction = { memo ->
                    abacusStateManager.transfer(
                        input = memo,
                        type = TransferInputField.MEMO,
                    )
                },
            ),
            addressInput = AddressInputBox.ViewState(
                localizer = localizer,
                formatter = formatter,
                parser = parser,
                value = transferInput?.address,
                placeholder = "dydx000...000000",
                onEditAction = { address ->
                    abacusStateManager.transfer(
                        input = address,
                        type = TransferInputField.address,
                    )
                },
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
