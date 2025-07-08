package exchange.dydx.trading.feature.profile.update

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxUpdateViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxUpdateView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxUpdateView.ViewState {
        val config = abacusStateManager.environment?.apps?.android
        return DydxUpdateView.ViewState(
            localizer = localizer,
            title = localizer.localize(config?.title ?: "FORCED_UPDATE.TITLE"),
            text = localizer.localize(config?.text ?: "FORCED_UPDATE.TEXT"),
            buttonTitle = localizer.localize(config?.action ?: "FORCED_UPDATE.ACTION"),
            buttonAction = {
                config?.url?.let {
                    router.navigateTo(it)
                }
            },
        )
    }
}
