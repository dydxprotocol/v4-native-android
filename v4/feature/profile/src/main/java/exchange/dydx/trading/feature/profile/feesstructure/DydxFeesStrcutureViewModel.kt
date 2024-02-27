package exchange.dydx.trading.feature.profile.feesstructure

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.output.User
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxFeesStrcutureViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxFeesStrcutureView.ViewState?> =
        combine(
            abacusStateManager.state.configs.map { it?.feeTiers }.distinctUntilChanged(),
            abacusStateManager.state.user,
        ) { feeTiers, user ->
            createViewState(feeTiers, user)
        }
            .distinctUntilChanged()

    private fun createViewState(
        feeTiers: List<FeeTier>?,
        user: User?
    ): DydxFeesStrcutureView.ViewState {
        val tradingVolume = if (user != null) {
            formatter.dollarVolume(user.makerVolume30D + user.takerVolume30D)
        } else {
            null
        }
        return DydxFeesStrcutureView.ViewState(
            localizer = localizer,
            backButtonAction = {
                router.navigateBack()
            },
            tradingVolume = tradingVolume,
            items = feeTiers?.map {
                createItem(it, user?.feeTierId)
            } ?: emptyList(),
        )
    }

    private fun createItem(
        tier: FeeTier,
        userAtTierId: String?,
    ): DydxFeesItemView.ViewState {
        val conditions: MutableList<DydxFeesItemView.Condition> = mutableListOf()
        val volume = formatter.condensed(tier.volume.toDouble(), 0)
        if (volume != null) {
            conditions.add(
                DydxFeesItemView.Condition(
                    title = null,
                    value = "${tier.symbol} $volume",
                ),
            )
        }
        tier.totalShare?.let {
            if (it > 0.0) {
                val value = formatter.percent(it, 3)
                if (value != null) {
                    conditions.add(
                        DydxFeesItemView.Condition(
                            title = localizer.localize("APP.FEE_TIERS.AND_EXCHANGE_MARKET_SHARE"),
                            value = "> $value",
                        ),
                    )
                }
            }
        }
        tier.makerShare?.let {
            if (it > 0.0) {
                val value = formatter.percent(it, 3)
                if (value != null) {
                    conditions.add(
                        DydxFeesItemView.Condition(
                            title = localizer.localize("APP.FEE_TIERS.AND_MAKER_MARKET_SHARE"),
                            value = "> $value",
                        ),
                    )
                }
            }
        }

        return DydxFeesItemView.ViewState(
            localizer = localizer,
            tier = tier.id,
            conditions = conditions,
            makerPercent = formatter.percent(tier.maker, 3),
            takerPercent = formatter.percent(tier.taker, 3),
            isUserTier = tier.id == userAtTierId,
        )
    }
}
