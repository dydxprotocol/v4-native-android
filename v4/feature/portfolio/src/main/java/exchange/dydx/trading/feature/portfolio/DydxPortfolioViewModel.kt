package exchange.dydx.trading.feature.portfolio

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.feature.portfolio.components.overview.DydxPortfolioSectionsView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val displayContent: Flow<@JvmSuppressWildcards DydxPortfolioView.DisplayContent>,
    private val tabSelection: Flow<@JvmSuppressWildcards DydxPortfolioSectionsView.Selection>,
    private val featureFlags: DydxFeatureFlags
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioView.ViewState?> =
        combine(
            displayContent,
            tabSelection,
        ) { displayContent, tabSelection ->
            createViewState(displayContent, tabSelection)
        }
            .distinctUntilChanged()

    private fun createViewState(
        displayContent: DydxPortfolioView.DisplayContent,
        tabSelection: DydxPortfolioSectionsView.Selection,
    ): DydxPortfolioView.ViewState {
        return DydxPortfolioView.ViewState(
            localizer = localizer,
            displayContent = displayContent,
            tabSelection = tabSelection,
            vaultEnabled = featureFlags.isFeatureEnabled(DydxFeatureFlag.vault_enabled, default = true),
        )
    }
}
