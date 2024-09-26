package exchange.dydx.trading.feature.vault.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxVaultHeaderViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultHeaderView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxVaultHeaderView.ViewState {
        return DydxVaultHeaderView.ViewState(
            localizer = localizer,
            dydxChainLogoUrl = abacusStateManager.environment?.chainLogo,
            learnMoreAction = {
                val url = abacusStateManager.environment?.links?.vaultLearnMore
                if (url != null) {
                    router.navigateTo(url)
                }
            }
        )
    }
}
