package exchange.dydx.trading.feature.transfer.search

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxBoolFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.DydxRouter.Presentation
import exchange.dydx.trading.common.navigation.TransferRoutes
import exchange.dydx.trading.feature.shared.TransferTokenDetails
import exchange.dydx.trading.feature.shared.TransferTokenInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class DydxInstantDepositSearchViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val transferTokenDetails: TransferTokenDetails,
    private val router: DydxRouter,
    private val featureFlags: DydxFeatureFlags,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxInstantDepositSearchView.ViewState?> =
        combine(
            transferTokenDetails.infos,
            transferTokenDetails.selectedToken,
            transferTokenDetails.defaultToken,
        ) { infos, selectedToken, defaultToken ->
            createViewState(
                infos = infos,
                selectedToken = selectedToken ?: defaultToken,
            )
        }

    private fun createViewState(
        infos: List<TransferTokenInfo>,
        selectedToken: TransferTokenInfo?,
    ): DydxInstantDepositSearchView.ViewState {
        var tokens = mutableListOf<DydxInstantDepositSearchItem.ViewState>()
        var otherTokens = mutableListOf<DydxInstantDepositSearchItem.ViewState>()
        for (token in infos) {
            val itemViewState = createItemViewModel(
                token = token,
                selected = selectedToken,
            )
            if ((token.amount ?: 0.0) > 0.0 || (token.usdcAmount ?: 0.0) > 0.0) {
                tokens.add(itemViewState)
            } else {
                otherTokens.add(itemViewState)
            }
        }
        return DydxInstantDepositSearchView.ViewState(
            localizer = localizer,
            backButtonAction = {
                router.navigateBack()
            },
            tokens = tokens,
            otherTokens = otherTokens,
            nobleItem = DydxTransferNobleItemView.ViewState(
                localizer = localizer,
                nobleAdddressAction = {
                    router.navigateTo(TransferRoutes.transfer_deposit_noble, presentation = Presentation.Push)
                },
            ),
            fiatItem = if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_fiat_deposit)) {
                DydxTransferFiatItemView.ViewState(
                    localizer = localizer,
                    selectAction = {
                        router.navigateTo(TransferRoutes.transfer_fiat_deposit, presentation = Presentation.Push)
                    },
                )
            } else {
                null
            },
        )
    }

    private fun createItemViewModel(
        token: TransferTokenInfo,
        selected: TransferTokenInfo?,
    ): DydxInstantDepositSearchItem.ViewState {
        return DydxInstantDepositSearchItem.ViewState(
            localizer = localizer,
            token = token.token.name,
            chain = token.chain.name,
            tokenIconUri = token.tokenLogoUrl(abacusStateManager.deploymentUri),
            chainIconUri = token.chainLogUrl(abacusStateManager.deploymentUri),
            tokenSize = if (token.amount != null) {
                formatter.raw(token.amount, digits = 4)
            } else {
                null
            },
            usdcSize = if (token.usdcAmount != null) {
                formatter.dollar(token.usdcAmount, digits = 2)
            } else {
                null
            },
            isSelected = selected?.chainId == token.chainId && selected.tokenAddress == token.tokenAddress,
            selectAction = {
                if ((token.amount ?: 0.0) > 0.0) {
                    transferTokenDetails.selectedToken.value = token
                    router.navigateBack()
                }
            },
        )
    }
}
