package exchange.dydx.trading.feature.portfolio

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxBoolFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.feature.portfolio.components.overview.DydxPortfolioSectionsView
import exchange.dydx.trading.feature.shared.apprating.AppRatingDialog
import exchange.dydx.trading.feature.shared.apprating.AppRatingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxPortfolioViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val displayContent: Flow<@JvmSuppressWildcards DydxPortfolioView.DisplayContent>,
    private val tabSelection: Flow<@JvmSuppressWildcards DydxPortfolioSectionsView.Selection>,
    private val featureFlags: DydxFeatureFlags,
    private val appRatingState: AppRatingState,
) : ViewModel(), DydxViewModel {

    private val appRatingDialog = AppRatingDialog(
        localizer = localizer,
        onDismiss = {
            appRatingState.prompted(AppRatingState.ResponseType.DISMISSED)
        },
        onPositiveClick = {
            appRatingState.prompted(AppRatingState.ResponseType.POSITIVE)
        },
        onNegativeClick = {
            appRatingState.prompted(AppRatingState.ResponseType.NEGATIVE)
        },
        showing = MutableStateFlow(false),
    )

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
        if (appRatingState.shouldShowDialog) {
            appRatingDialog.showing.value = true
        }
        return DydxPortfolioView.ViewState(
            localizer = localizer,
            displayContent = displayContent,
            tabSelection = tabSelection,
            vaultEnabled = featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.vault_enabled),
            appRatingDialog = appRatingDialog,
        )
    }
}
