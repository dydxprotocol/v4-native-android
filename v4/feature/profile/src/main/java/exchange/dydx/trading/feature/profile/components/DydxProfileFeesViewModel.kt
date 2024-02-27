package exchange.dydx.trading.feature.profile.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.User
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.ProfileRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxProfileFeesViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxProfileFeesView.ViewState?> =
        abacusStateManager.state.user
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        user: User?
    ): DydxProfileFeesView.ViewState {
        return DydxProfileFeesView.ViewState(
            localizer = localizer,
            tradingVolume = user?.let {
                formatter.dollarVolume(user.makerVolume30D + user.takerVolume30D, 2)
            },
            takerFeeRate = user?.let {
                formatter.percent(user.takerFeeRate, 3)
            },
            makerFeeRate = user?.let {
                formatter.percent(user.makerFeeRate, 3)
            },
            tapAction = {
                router.navigateTo(
                    route = ProfileRoutes.fees_structure,
                    presentation = DydxRouter.Presentation.Push,
                )
            },
        )
    }
}
