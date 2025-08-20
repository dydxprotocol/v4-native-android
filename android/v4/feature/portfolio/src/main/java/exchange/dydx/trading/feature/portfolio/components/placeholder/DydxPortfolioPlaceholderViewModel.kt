package exchange.dydx.trading.feature.portfolio.components.placeholder

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxBoolFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
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
    private val featureFlags: DydxFeatureFlags,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioPlaceholderView.ViewState?> =
        combine(
            abacusStateManager.state.onboarded,
            abacusStateManager.state.selectedSubaccount,
            tabSelection,
            abacusStateManager.state.currentWallet,
        ) { onboarded, subaccount, tabSelection, currentWallet ->
            createViewState(onboarded, subaccount, tabSelection, currentWallet)
        }
            .distinctUntilChanged()

    private fun createViewState(
        onboarded: Boolean,
        subaccount: Subaccount?,
        tabSelection: DydxPortfolioPlaceholderView.Selection,
        wallet: DydxWalletInstance?
    ): DydxPortfolioPlaceholderView.ViewState {
        return DydxPortfolioPlaceholderView.ViewState(
            localizer = localizer,
            onboardState = if (onboarded) {
                if ((subaccount?.freeCollateral?.current ?: 0.0) > 0.0) {
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
                DydxPortfolioPlaceholderView.Selection.Funding -> {
                    if (onboarded) {
                        localizer.localize("APP.GENERAL.PLACEHOLDER_NO_FUNDING")
                    } else {
                        localizer.localize("APP.GENERAL.PLACEHOLDER_NO_FUNDING_LOG_IN")
                    }
                }
            },
            onboardTapAction = {
                router.navigateTo(
                    route = OnboardingRoutes.landing(featureFlags = featureFlags),
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
            transferTapAction = {
                if (featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_turnkey_android)) {
                    router.navigateTo(
                        route = if (wallet?.walletId == "turnkey") {
                            TransferRoutes.transfer_turnkey_deposit
                        } else {
                            TransferRoutes.transfer_deposit
                        },
                        presentation = DydxRouter.Presentation.Modal,
                    )
                } else {
                    router.navigateTo(
                        route = TransferRoutes.transfer,
                        presentation = DydxRouter.Presentation.Modal,
                    )
                }
            },
        )
    }
}
