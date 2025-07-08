package exchange.dydx.trading.feature.vault.depositwithdraw.confirmation

import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.combine
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
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.PreferenceKeys
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
import exchange.dydx.trading.feature.vault.depositwithdraw.components.VaultTosCheckbox
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.SharedPreferencesStore
import exchange.dydx.utilities.utils.applyLink
import indexer.models.chain.ChainError
import indexer.models.chain.OnChainTransactionErrorResponse
import indexer.models.chain.OnChainTransactionSuccessResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val sharedPreferencesStore: SharedPreferencesStore,
) : ViewModel(), DydxViewModel {

    private val isSubmitting: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isDepositTosAccepted: Boolean =
        sharedPreferencesStore.read(PreferenceKeys.VaultTosAccepted, defaultValue = "false") == "true"

    val state: Flow<DydxVaultConfirmationView.ViewState?> =
        combine(
            inputState.amount,
            inputState.type.filterNotNull(),
            inputState.result,
            inputState.slippageAcked,
            isSubmitting,
            inputState.tosAcked,
        ) { amount, type, result, slippageAcked, isSubmitting, isDepositTosChecked ->
            createViewState(amount, type, result, slippageAcked, isSubmitting, isDepositTosChecked)
        }

    init {
        inputState.tosAcked.value = isDepositTosAccepted
    }

    private fun createViewState(
        amount: Double?,
        type: VaultInputType,
        result: VaultFormValidationResult?,
        slippageAcked: Boolean,
        isSubmitting: Boolean,
        isDepositTosChecked: Boolean,
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
                VaultInputType.DEPOSIT -> localizer.localize("APP.VAULTS.MEGAVAULT")
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
            ctaButton = createInputCtaButton(
                type,
                result = result,
                isSubmitting = isSubmitting,
                isDepositTosChecked = isDepositTosChecked,
                slippageAcked = slippageAcked,
            ),
            slippage = createSlippage(
                type = type,
                result = result,
                slippageAcked = slippageAcked,
            ),
            tos = if (!isDepositTosAccepted) {
                createTos(
                    type = type,
                    isDepositTosChecked = isDepositTosChecked,
                )
            } else {
                null
            },
        )
    }

    private fun createSlippage(
        type: VaultInputType,
        result: VaultFormValidationResult?,
        slippageAcked: Boolean,
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
                checked = slippageAcked,
                onCheckedChange = { inputState.slippageAcked.value = it },
            )
        } else {
            return null
        }
    }

    private fun createTos(
        type: VaultInputType,
        isDepositTosChecked: Boolean,
    ): VaultTosCheckbox.ViewState? {
        val ctaButtonTitle = ctaButtonTitle(type)
        val agreementText = localizer.localizeWithParams(
            "APP.VAULTS.MEGAVAULT_TERMS_TEXT",
            mapOf("CONFIRM_BUTTON_TEXT" to ctaButtonTitle),
        )
        val value = buildAnnotatedString {
            val newString = applyLink(
                value = agreementText,
                key = "{LINK}",
                replacement = localizer.localize("APP.VAULTS.MEGAVAULT_TERMS_LINK_TEXT"),
                link = abacusStateManager.environment?.links?.vaultTos,
                linkColor = ThemeColor.SemanticColor.color_purple.color,
            )
            append(newString)
        }
        return if (type == VaultInputType.DEPOSIT) {
            VaultTosCheckbox.ViewState(
                localizer = localizer,
                text = value,
                checked = isDepositTosChecked,
                onCheckedChange = { checked ->
                    inputState.tosAcked.value = checked
                    sharedPreferencesStore.save(if (checked) "true" else "false", PreferenceKeys.VaultTosAccepted)
                },
                linkAction = {
                    val url = abacusStateManager.environment?.links?.vaultTos
                    if (url != null) {
                        router.navigateTo(url)
                    }
                },
            )
        } else {
            null
        }
    }

    private fun createInputCtaButton(
        type: VaultInputType,
        result: VaultFormValidationResult?,
        isSubmitting: Boolean,
        isDepositTosChecked: Boolean,
        slippageAcked: Boolean,
    ): InputCtaButton.ViewState {
        val ctaButtonTitle = ctaButtonTitle(type)
        when (type) {
            VaultInputType.DEPOSIT -> {
                return InputCtaButton.ViewState(
                    localizer = localizer,
                    ctaButtonState = if (isSubmitting) {
                        InputCtaButton.State.Disabled(localizer.localize("APP.TRADE.SUBMITTING"))
                    } else if (!isDepositTosChecked) {
                        InputCtaButton.State.Disabled(localizer.localize("APP.VAULTS.ACKNOWLEDGE_MEGAVAULT_TERMS"))
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
                return InputCtaButton.ViewState(
                    localizer = localizer,
                    ctaButtonState = if (isSubmitting) {
                        InputCtaButton.State.Disabled(localizer.localize("APP.TRADE.SUBMITTING"))
                    } else if (result?.summaryData?.needSlippageAck == true && !slippageAcked) {
                        InputCtaButton.State.Disabled(localizer.localize("APP.VAULTS.ACKNOWLEDGE_HIGH_SLIPPAGE"))
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

    private fun ctaButtonTitle(type: VaultInputType): String {
        return when (type) {
            VaultInputType.DEPOSIT -> localizer.localize("APP.VAULTS.CONFIRM_DEPOSIT_CTA")
            VaultInputType.WITHDRAW -> localizer.localize("APP.VAULTS.CONFIRM_WITHDRAW_CTA")
        }
    }

    private fun submitDeposit(depositData: VaultDepositData?, amount: Double?) {
        if (depositData == null) {
            return
        }
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
        if (withdrawData == null) {
            return
        }
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
