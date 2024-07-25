package exchange.dydx.trading.feature.profile.rewards

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.Account
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.nativeTokenLogoUrl
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DydxRewardsEventsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {
    private val periodsValues = listOf(
        HistoricalTradingRewardsPeriod.DAILY,
        HistoricalTradingRewardsPeriod.WEEKLY,
        HistoricalTradingRewardsPeriod.MONTHLY,
    )
    private val periodsText = listOf(
        localizer.localize("APP.GENERAL.TIME_STRINGS.DAILY"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.WEEKLY"),
        localizer.localize("APP.GENERAL.TIME_STRINGS.MONTHLY"),
    )
    private var selectedPeriod =
        MutableStateFlow<HistoricalTradingRewardsPeriod>(HistoricalTradingRewardsPeriod.WEEKLY)

    val state: Flow<DydxRewardsEventsView.ViewState?> = combine(
        abacusStateManager.state.account,
        selectedPeriod,
    ) { account, selectedPeriod ->
        createViewState(account, selectedPeriod)
    }
        .distinctUntilChanged()

    private fun createViewState(
        account: Account?,
        selectedPeriod: HistoricalTradingRewardsPeriod
    ): DydxRewardsEventsView.ViewState {
        val selectedIndex = periodsValues.indexOf(selectedPeriod)
        val selectedPeriodText = if (selectedIndex != -1) this.periodsValues[selectedIndex].rawValue else ""

        return DydxRewardsEventsView.ViewState(
            localizer = localizer,
            title = localizer.localize("APP.GENERAL.TRADING_REWARDS"),
            periods = this.periodsText,
            selectedIndex = selectedIndex,
            rewards = account?.tradingRewards?.filledHistory?.get(selectedPeriodText)?.map { reward ->
                val started = Instant.ofEpochMilli(reward.startedAtInMilliseconds.toLong())
                val ended = reward.endedAtInMilliseconds.toLong().let {
                    Instant.ofEpochMilli(it)
                }
                val startedTimeText = formatter.utcDate(started)
                val timeText = startedTimeText?.let { start ->
                    when (selectedPeriod) {
                        HistoricalTradingRewardsPeriod.DAILY -> start
                        HistoricalTradingRewardsPeriod.WEEKLY, HistoricalTradingRewardsPeriod.MONTHLY -> {
                            val endedTimeText = formatter.utcDate(ended.minusSeconds(1))
                            endedTimeText?.let { end ->
                                "$start - $end"
                            } ?: start
                        }
                    }
                }

                DydxRewardsEventItemView.ViewState(
                    timeText = timeText ?: "",
                    amountText = formatter.raw(reward.amount, 6) ?: "",
                    nativeTokenLogoUrl = abacusStateManager.nativeTokenLogoUrl,
                )
            }
                ?: listOf(),
            onPeriodChanged = { index ->
                val period = periodsValues[index]
                this.selectedPeriod.value = period
                abacusStateManager.setHistoricalTradingRewardsPeriod(period)
            },
        )
    }
}
