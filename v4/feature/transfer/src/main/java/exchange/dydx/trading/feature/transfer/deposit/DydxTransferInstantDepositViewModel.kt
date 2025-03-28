package exchange.dydx.trading.feature.transfer.deposit

import android.R.attr.type
import android.R.id.input
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.dydxstatemanager.AbacusStateManager
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.feature.shared.TransferTokenDetails
import exchange.dydx.trading.feature.shared.TransferTokenInfo
import exchange.dydx.trading.feature.transfer.components.InstantInputBox
import exchange.dydx.trading.feature.transfer.components.InstantSelector
import exchange.dydx.trading.feature.transfer.components.TransferRouteSelection
import jnr.ffi.provider.jffi.CodegenUtils.params
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.selects.select
import java.net.URL
import javax.inject.Inject
import kotlin.String
import kotlin.math.min

@HiltViewModel
class DydxTransferInstantDepositViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val transferTokenDetails: TransferTokenDetails,
    private val router: DydxRouter,
    private val parser: ParserProtocol,
) : ViewModel(), DydxViewModel {

    private var currentSize: Double? = null

    val state: Flow<DydxTransferInstantDepositView.ViewState?> =
        combine(
            abacusStateManager.state.transferInput,
            transferTokenDetails.selectedToken,
            transferTokenDetails.defaultToken,
            abacusStateManager.state.currentWallet.map { it?.ethereumAddress }.distinctUntilChanged(),
        ) { transferInput, selectedToken, defaultToken, ethereumAddress ->
            createViewState(
                transferInput = transferInput,
                selectedToken = selectedToken,
                defaultToken = defaultToken,
                showConnectWallet = ethereumAddress.isNullOrEmpty()
            )
        }
            .distinctUntilChanged()

    private fun createViewState(
        transferInput: TransferInput?,
        selectedToken: TransferTokenInfo?,
        defaultToken: TransferTokenInfo?,
        showConnectWallet: Boolean,
    ): DydxTransferInstantDepositView.ViewState {
        val token = selectedToken ?: defaultToken
        val decimal = parser.asString(token?.decimals)
        if (decimal != null) {
            abacusStateManager.transfer(input = decimal, type = TransferInputField.decimals)
        }
        if (transferInput?.chain != token?.chainId) {
            abacusStateManager.transfer(input = token?.chainId, type = TransferInputField.chain)
        }
        if (transferInput?.token != token?.tokenAddress) {
            abacusStateManager.transfer(input = token?.tokenAddress, type = TransferInputField.token)
        }
        return DydxTransferInstantDepositView.ViewState(
            localizer = localizer,
            inputBox = createInputTokenState(
                transferInput = transferInput,
                token = token,
            ),
            selector =  createSelectorState(
                transferInput = transferInput,
                token = token,
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

    private fun createSelectorState(
        transferInput: TransferInput?,
        token: TransferTokenInfo?,
    ): InstantSelector.ViewState {
        var regularTime = "< " + localizer.localize("APP.GENERAL.TIME_STRINGS.30MIN")
        transferInput?.summary?.estimatedRouteDurationSeconds?.toDouble()?.let {
            if (it > 0) {
                val minutes = parser.asString((it / 60).toInt())
                if (minutes != null) {
                    regularTime = localizer.localizeWithParams("APP.GENERAL.TIME_STRINGS.X_MINUTES", params = mapOf("X" to minutes))
                }
            }
        }

        var regularFee: String =  localizer.localize("APP.ONBOARDING.SKIP_SLOW_ROUTE_DESC")
        transferInput?.summary?.bridgeFee?.toDouble()?.let {
            if (it > 0) {
                formatter.dollar(it, digits = 2)?.let {
                    regularFee = it
                }
            }
        }

        var instantFee: String =  localizer.localize("APP.GENERAL.UNAVAILABLE")
        transferInput?.goFastSummary?.bridgeFee?.toDouble()?.let {
            if (it > 0) {
                formatter.dollar(it, digits = 2)?.let {
                    instantFee = it
                }
            }
        }

        return InstantSelector.ViewState(
            localizer = localizer,
            selection = TransferRouteSelection.Instant,
            instantFee = instantFee,
            regularTime = regularTime,
            regularFee = regularFee,
            selectionAction = {
                // Handle selection action
            }
        )
    }

    private fun createInputTokenState(
        transferInput: TransferInput?,
        token: TransferTokenInfo?,
    ): InstantInputBox.ViewState {
        val value: String? = transferInput?.size?.size?.let {
            val valueDouble = parser.asDouble(it) ?: 0.0
            if (valueDouble == 0.0) {
                null
            } else {
                formatter.raw(valueDouble, digits = 4)
            }
        }

        return InstantInputBox.ViewState(
            localizer = localizer,
            value = value,
            valuePlaceholder = formatter.raw(0.0, digits = 4),
            token = token?.token?.name.toString(),
            maxAmount = token?.amount,
            maxAmountString = formatter.raw(token?.amount, digits = 4),
            tokenIconUri = token?.tokenLogoUrl(abacusStateManager.deploymentUri),
            chainIconUri = token?.chainLogUrl(abacusStateManager.deploymentUri),
            assetAction = {
                router.navigateTo(route = "/transfer/deposit/search", presentation = DydxRouter.Presentation.Push)
            },
            maxAction = {
                val amount = parser.asString(token?.amount)
                abacusStateManager.transfer(input = amount, type = TransferInputField.size)
            },
            editAction = { value ->
                val size = min(parser.asDouble(value) ?: 0.0, token?.amount ?: 0.0)
                if (size != currentSize) {
                    abacusStateManager.transfer(input = parser.asString(size), type = TransferInputField.size)
                    currentSize = size
                }
            }
        )
    }
}
