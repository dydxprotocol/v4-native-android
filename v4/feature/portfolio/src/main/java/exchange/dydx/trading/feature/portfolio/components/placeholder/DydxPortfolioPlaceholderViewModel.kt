package exchange.dydx.trading.feature.portfolio.components.placeholder

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import exchange.dydx.trading.common.navigation.TransferRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioPlaceholderViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val tabSelection: Flow<@JvmSuppressWildcards DydxPortfolioPlaceholderView.Selection>,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioPlaceholderView.ViewState?> =
        combine(
            abacusStateManager.state.onboarded,
            abacusStateManager.state.selectedSubaccount,
            tabSelection,
        ) { onboarded, subaccount, tabSelection ->
            createViewState(onboarded, subaccount, tabSelection)
        }
            .distinctUntilChanged()

    private fun createViewState(
        onboarded: Boolean,
        subaccount: Subaccount?,
        tabSelection: DydxPortfolioPlaceholderView.Selection,
    ): DydxPortfolioPlaceholderView.ViewState {
        return DydxPortfolioPlaceholderView.ViewState(
            localizer = localizer,
            onboardState = if (onboarded) {
                if (subaccount?.freeCollateral?.current ?: 0.0 > 0.0) {
                    DydxPortfolioPlaceholderView.OnboardState.Ready
                } else {
                    DydxPortfolioPlaceholderView.OnboardState.NeedDeposit
                }
            } else {
                DydxPortfolioPlaceholderView.OnboardState.NeedWallet
            },
            placeholderText = when (tabSelection) {
                DydxPortfolioPlaceholderView.Selection.Positions -> {
                    if (onboarded) {
                        localizer.localize("APP.GENERAL.PLACEHOLDER_NO_POSITIONS")
                    } else {
                        localizer.localize("APP.GENERAL.PLACEHOLDER_NO_POSITIONS_LOG_IN")
                    }
                }
                DydxPortfolioPlaceholderView.Selection.Orders -> {
                    if (onboarded) {
                        localizer.localize("APP.GENERAL.PLACEHOLDER_NO_ORDERS")
                    } else {
                        localizer.localize("APP.GENERAL.PLACEHOLDER_NO_ORDERS_LOG_IN")
                    }
                }
                DydxPortfolioPlaceholderView.Selection.Trades -> {
                    if (onboarded) {
                        localizer.localize("APP.GENERAL.PLACEHOLDER_NO_FILLS")
                    } else {
                        localizer.localize("APP.GENERAL.PLACEHOLDER_NO_FILLS_LOG_IN")
                    }
                }
                DydxPortfolioPlaceholderView.Selection.Transfer -> {
                    if (onboarded) {
                        localizer.localize("APP.GENERAL.PLACEHOLDER_NO_TRANSFERS")
                    } else {
                        localizer.localize("APP.GENERAL.PLACEHOLDER_NO_TRANSFERS_LOG_IN")
                    }
                }
                else -> ""
            },
            onboardTapAction = {
                router.navigateTo(
                    route = OnboardingRoutes.wallet_list,
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            transferTapAction = {
                router.navigateTo(
                    route = TransferRoutes.transfer,
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
        )
    }
}
