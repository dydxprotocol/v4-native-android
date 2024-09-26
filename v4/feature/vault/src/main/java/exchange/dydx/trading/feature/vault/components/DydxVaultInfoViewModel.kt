package exchange.dydx.trading.feature.vault.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxVaultInfoViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultInfoView.ViewState?> = abacusStateManager.state.vault
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(vault: Vault?): DydxVaultInfoView.ViewState {
        val thirtyDayReturnPercent = vault?.details?.thirtyDayReturnPercent
        val apr = if (thirtyDayReturnPercent != null) {
            val text = formatter.percent(thirtyDayReturnPercent, 2)
            if (thirtyDayReturnPercent > 0) {
                SignedAmountView.ViewState(
                    sign = PlatformUISign.Plus,
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                    text = text,
                )
            } else if (thirtyDayReturnPercent < 0) {
                SignedAmountView.ViewState(
                    sign = PlatformUISign.Minus,
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                    text = text,
                )
            } else {
                null
            }
        } else {
            null
        }
        return DydxVaultInfoView.ViewState(
            localizer = localizer,
            balance = "$1.0M",
            pnl = "$100.0",
            apr = apr,
            tvl = formatter.dollar(vault?.details?.totalValue),
        )
    }
}
