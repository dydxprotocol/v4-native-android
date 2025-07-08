package exchange.dydx.trading.feature.portfolio

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.DydxBoolFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.navigation.DydxRouter
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
    private val router: DydxRouter,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    private val appRatingDialog = AppRatingDialog(
        localizer = localizer,
        onDismiss = {
            appRatingState.prompted(AppRatingState.ResponseType.DISMISSED)
        },
        onPositiveClick = {
            appRatingState.prompted(AppRatingState.ResponseType.POSITIVE)
            shouldLaunchAppRatingFlow.value = true
        },
        onNegativeClick = {
            appRatingState.prompted(AppRatingState.ResponseType.NEGATIVE)
            val url = abacusStateManager.environment?.links?.feedback
            if (url != null) {
                router.navigateTo(url)
            }
        },
        showing = MutableStateFlow(false),
    )

    private val shouldLaunchAppRatingFlow = MutableStateFlow(false)

    val state: Flow<DydxPortfolioView.ViewState?> =
        combine(
            displayContent,
            tabSelection,
            shouldLaunchAppRatingFlow,
        ) { displayContent, tabSelection, shouldLaunchAppRating ->
            createViewState(displayContent, tabSelection, shouldLaunchAppRating)
        }
            .distinctUntilChanged()

    private fun createViewState(
        displayContent: DydxPortfolioView.DisplayContent,
        tabSelection: DydxPortfolioSectionsView.Selection,
        shouldLaunchAppRating: Boolean,
    ): DydxPortfolioView.ViewState {
        if (appRatingState.shouldShowDialog) {
            appRatingState.prompt()
            appRatingDialog.showing.value = true
        }
        return DydxPortfolioView.ViewState(
            localizer = localizer,
            displayContent = displayContent,
            tabSelection = tabSelection,
            vaultEnabled = featureFlags.isFeatureEnabled(DydxBoolFeatureFlag.ff_vault_enabled),
            appRatingDialog = appRatingDialog,
            shouldLaunchAppRating = shouldLaunchAppRating,
        )
    }

    fun launchAppRating(context: Context) {
        if (shouldLaunchAppRatingFlow.value) {
            appRatingState.startReviewFlow(context)
            shouldLaunchAppRatingFlow.value = false
        }
    }
}
