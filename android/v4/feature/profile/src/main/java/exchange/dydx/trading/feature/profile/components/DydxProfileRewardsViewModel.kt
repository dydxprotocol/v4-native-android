package exchange.dydx.trading.feature.profile.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.Account
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.nativeTokenLogoUrl
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.ProfileRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DydxProfileRewardsViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    init {
        abacusStateManager.setHistoricalTradingRewardsPeriod(HistoricalTradingRewardsPeriod.WEEKLY)
    }

    val state: Flow<DydxProfileRewardsView.ViewState?> = abacusStateManager.state.account
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(account: Account?): DydxProfileRewardsView.ViewState {
        val rewards = account?.tradingRewards
        val total = rewards?.total
        val thisWeek = rewards?.filledHistory?.get("WEEKLY")?.lastOrNull()
        val thisWeekAmount = thisWeek?.amount
        val thisWeekStart = thisWeek?.startedAtInMilliseconds?.toLong()?.let {
            Instant.ofEpochMilli(it)
        }
        val thisWeekStartText = thisWeekStart?.let { formatter.utcDate(it) }
        val thisWeekEnd = thisWeek?.endedAtInMilliseconds?.toLong()?.let {
            Instant.ofEpochMilli(it)
        }
        val thisWeekEndText = thisWeekEnd?.let { formatter.utcDate(it) }
        return DydxProfileRewardsView.ViewState(
            localizer = localizer,
            summary = DydxRewardsSummaryState(
                titleText = localizer.localize("APP.GENERAL.TRADING_REWARDS"),
                rewards7DaysText = formatter.raw(thisWeekAmount, 6),
                range7DaysText = thisWeekStartText?.let { start ->
                    thisWeekEndText?.let { end ->
                        "$start - $end"
                    } ?: "$start - "
                } ?: "",
                rewardsAllTimeText = formatter.raw(total, 6),
            ),
            nativeTokenLogoUrl = abacusStateManager.nativeTokenLogoUrl,
            onTapAction = {
                router.navigateTo(
                    route = ProfileRoutes.rewards,
                    presentation = DydxRouter.Presentation.Push,
                )
            },
        )
    }
}
