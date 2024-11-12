package exchange.dydx.trading.feature.portfolio.components.vault

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.VaultRoutes
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class DydxPortfolioVaultViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioVaultView.ViewState?> = abacusStateManager.state.vault
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(vault: Vault?): DydxPortfolioVaultView.ViewState? {
        val account = vault?.account ?: return null
        val apr = vault?.details?.thirtyDayReturnPercent
        return DydxPortfolioVaultView.ViewState(
            localizer = localizer,
            balance = formatter.dollar(account.balanceUsdc, digits = 2),
            apr = SignedAmountView.ViewState(
                text = formatter.percent(apr?.absoluteValue, digits = 2),
                sign = if ((apr ?: 0.0) > 0) {
                    PlatformUISign.Plus
                } else if ((apr ?: 0.0) < 0) {
                    PlatformUISign.Minus
                } else {
                    PlatformUISign.None
                },
            ),
            onTapAction = {
                router.tabTo(VaultRoutes.main)
            },
        )
    }
}
