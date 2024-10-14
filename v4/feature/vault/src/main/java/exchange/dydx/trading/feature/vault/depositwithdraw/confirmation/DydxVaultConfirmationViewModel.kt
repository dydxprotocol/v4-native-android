package exchange.dydx.trading.feature.vault.depositwithdraw.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultDepositData
import exchange.dydx.abacus.functional.vault.VaultFormValidationResult
import exchange.dydx.abacus.functional.vault.VaultWithdrawData
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.analytics.VaultAnalytics
import exchange.dydx.trading.feature.shared.analytics.VaultAnalyticsInputType
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.vault.VaultInputStage
import exchange.dydx.trading.feature.vault.VaultInputState
import exchange.dydx.trading.feature.vault.VaultInputType
import exchange.dydx.trading.feature.vault.canDeposit
import exchange.dydx.trading.feature.vault.canWithdraw
import exchange.dydx.trading.feature.vault.depositwithdraw.components.VaultSlippageCheckbox
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import indexer.models.chain.ChainError
import indexer.models.chain.OnChainTransactionErrorResponse
import indexer.models.chain.OnChainTransactionSuccessResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class DydxVaultConfirmationViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val cosmosClient: CosmosV4WebviewClientProtocol,
    private val inputState: VaultInputState,
    private val router: DydxRouter,
    private val parser: ParserProtocol,
    private val platformInfo: PlatformInfo,
    private val vaultAnalytics: VaultAnalytics,
) : ViewModel(), DydxViewModel {

    private val isSubmitting: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val state: Flow<DydxVaultConfirmationView.ViewState?> =
        combine(
            inputState.amount,
            inputState.type.filterNotNull(),
            inputState.result,
            isSubmitting,
        ) { amount, type, result, isSubmitting ->
            createViewState(amount, type, result, isSubmitting)
        }

    private fun createViewState(
        amount: Double?,
        type: VaultInputType,
        result: VaultFormValidationResult?,
        isSubmitting: Boolean,
    ): DydxVaultConfirmationView.ViewState {
        return DydxVaultConfirmationView.ViewState(
            localizer = localizer,
            direction = when (type) {
                VaultInputType.DEPOSIT -> DydxVaultConfirmationView.Direction.Deposit
                VaultInputType.WITHDRAW -> DydxVaultConfirmationView.Direction.Withdraw
            },
            headerTitle = when (type) {
                VaultInputType.DEPOSIT -> localizer.localize("APP.VAULTS.CONFIRM_DEPOSIT_CTA")
                VaultInputType.WITHDRAW -> localizer.localize("APP.VAULTS.CONFIRM_WITHDRAW_CTA")
            },
            sourceLabel = when (type) {
                VaultInputType.DEPOSIT -> localizer.localize("APP.VAULTS.AMOUNT_TO_DEPOSIT")
                VaultInputType.WITHDRAW -> localizer.localize("APP.VAULTS.AMOUNT_TO_WITHDRAW")
            },
            sourceValue = formatter.dollar(amount, digits = 2),
            destinationValue = when (type) {
                VaultInputType.DEPOSIT -> localizer.localize("APP.VAULTS.VAULT")
                VaultInputType.WITHDRAW -> localizer.localize("APP.VAULTS.CROSS_ACCOUNT")
            },
            destinationIcon = when (type) {
                VaultInputType.DEPOSIT -> R.drawable.vault_account_token
                VaultInputType.WITHDRAW -> R.drawable.vault_cross_token
            },
            backAction = {
                router.navigateBack()
                inputState.stage.value = VaultInputStage.EDIT
            },
            ctaButton = createInputCtaButton(type, result, isSubmitting),
            slippage = createSlippage(type, result),
        )
    }

    private fun createSlippage(
        type: VaultInputType,
        result: VaultFormValidationResult?
    ): VaultSlippageCheckbox.ViewState? {
        if (type == VaultInputType.WITHDRAW && result?.summaryData?.needSlippageAck == true) {
            val slippage = formatter.percent(result?.summaryData?.estimatedSlippage, digits = 2) ?: ""
            val slippageText = localizer.localizeWithParams(
                path = "APP.VAULTS.SLIPPAGE_ACK",
                params = mapOf("AMOUNT" to slippage),
            )
            return VaultSlippageCheckbox.ViewState(
                localizer = localizer,
                text = slippageText,
                checked = inputState.slippageAcked.value,
                onCheckedChange = { inputState.slippageAcked.value = it },
            )
        } else {
            return null
        }
    }

    private fun createInputCtaButton(
        type: VaultInputType,
        result: VaultFormValidationResult?,
        isSubmitting: Boolean,
    ): InputCtaButton.ViewState {
        when (type) {
            VaultInputType.DEPOSIT -> {
                val ctaButtonTitle = localizer.localize("APP.VAULTS.CONFIRM_DEPOSIT_CTA")
                return InputCtaButton.ViewState(
                    localizer = localizer,
                    ctaButtonState = if (isSubmitting) {
                        InputCtaButton.State.Disabled(localizer.localize("APP.TRADE.SUBMITTING"))
                    } else if (result?.canDeposit == true) {
                        InputCtaButton.State.Enabled(ctaButtonTitle)
                    } else {
                        InputCtaButton.State.Disabled(ctaButtonTitle)
                    },
                    ctaAction = {
                        vaultAnalytics.logOperationAttempt(
                            type = VaultAnalyticsInputType.DEPOSIT,
                            amount = result?.submissionData?.deposit?.amount,
                            slippage = null,
                        )
                        submitDeposit(
                            depositData = result?.submissionData?.deposit,
                            amount = result?.submissionData?.deposit?.amount,
                        )
                    },
                )
            }
            VaultInputType.WITHDRAW -> {
                val ctaButtonTitle = localizer.localize("APP.VAULTS.CONFIRM_WITHDRAW_CTA")
                return InputCtaButton.ViewState(
                    localizer = localizer,
                    ctaButtonState = if (isSubmitting) {
                        InputCtaButton.State.Disabled(localizer.localize("APP.TRADE.SUBMITTING"))
                    } else if (result?.canWithdraw == true) {
                        InputCtaButton.State.Enabled(ctaButtonTitle)
                    } else {
                        InputCtaButton.State.Disabled(ctaButtonTitle)
                    },
                    ctaAction = {
                        vaultAnalytics.logOperationAttempt(
                            type = VaultAnalyticsInputType.WITHDRAW,
                            amount = result?.summaryData?.estimatedAmountReceived,
                            slippage = result?.summaryData?.estimatedSlippage,
                        )
                        submitWithdraw(
                            withdrawData = result?.submissionData?.withdraw,
                            amount = result?.summaryData?.estimatedAmountReceived,
                        )
                    },
                )
            }
        }
    }

    private fun submitDeposit(depositData: VaultDepositData?, amount: Double?) {
        val depositData = depositData ?: return
        isSubmitting.value = true
        cosmosClient.depositToMegavault(
            subaccountNumber = parser.asInt(depositData.subaccountFrom) ?: 0,
            amountUsdc = depositData.amount,
            completion = { response ->
                handleResponse(
                    response = response,
                    type = VaultInputType.DEPOSIT,
                    amount = amount,
                )
            },
        )
    }

    private fun submitWithdraw(withdrawData: VaultWithdrawData?, amount: Double?) {
        val withdrawData = withdrawData ?: return
        isSubmitting.value = true
        cosmosClient.withdrawFromMegavault(
            subaccountNumber = parser.asInt(withdrawData.subaccountTo) ?: 0,
            shares = withdrawData.shares.toLong(),
            minAmount = withdrawData.minAmount.toLong(),
            completion = { response ->
                handleResponse(
                    response = response,
                    type = VaultInputType.WITHDRAW,
                    amount = amount,
                )
            },
        )
    }

    private fun handleResponse(
        response: String?,
        type: VaultInputType,
        amount: Double?
    ) {
        val success = OnChainTransactionSuccessResponse.fromPayload(response)
        if (success != null) {
            abacusStateManager.refreshVaultAccount()
            inputState.reset()
            routeToVault()
            platformInfo.show(
                title = when (type) {
                    VaultInputType.DEPOSIT -> localizer.localize("APP.V4_DEPOSIT.COMPLETED_TITLE")
                    VaultInputType.WITHDRAW -> localizer.localize("APP.V4_WITHDRAWAL.COMPLETED_TITLE")
                },
                message = when (type) {
                    VaultInputType.DEPOSIT -> localizer.localize("APP.V4_DEPOSIT.COMPLETED_TEXT_SHORT")
                    VaultInputType.WITHDRAW -> localizer.localize("APP.V4_WITHDRAWAL.COMPLETED_TEXT")
                },
            )

            vaultAnalytics.logOperationSuccess(
                type = when (type) {
                    VaultInputType.DEPOSIT -> VaultAnalyticsInputType.DEPOSIT
                    VaultInputType.WITHDRAW -> VaultAnalyticsInputType.WITHDRAW
                },
                amount = amount,
                amountDiff = when (type) {
                    VaultInputType.DEPOSIT -> null
                    VaultInputType.WITHDRAW -> abs((success.actualWithdrawalAmount ?: 0.0) - (amount ?: 0.0))
                },
            )
        } else {
            val error = OnChainTransactionErrorResponse.fromPayload(response)
            val errorMessage = error?.error?.message
            if (errorMessage != null) {
                platformInfo.show(
                    title = localizer.localize("APP.GENERAL.FAILED"),
                    message = errorMessage,
                    type = PlatformInfoViewModel.Type.Error,
                )
            } else {
                platformInfo.show(
                    title = localizer.localize("APP.GENERAL.FAILED"),
                    message = ChainError.unknownError.message,
                    type = PlatformInfoViewModel.Type.Error,
                )
            }
            isSubmitting.value = false

            vaultAnalytics.logOperationFailure(
                type = when (type) {
                    VaultInputType.DEPOSIT -> VaultAnalyticsInputType.DEPOSIT
                    VaultInputType.WITHDRAW -> VaultAnalyticsInputType.WITHDRAW
                },
            )
        }
    }

    private fun routeToVault() {
        viewModelScope.launch {
            router.navigateBack()
            router.navigateBack()
        }
    }
}
