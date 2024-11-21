package exchange.dydx.trading.feature.profile.rewards

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Documentation
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxRewardsFaqsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {
    var expanded: MutableStateFlow<Set<String>> = MutableStateFlow(mutableSetOf<String>())

    val state: Flow<DydxRewardsFaqsView.ViewState?> = combine(
        abacusStateManager.state.documentation,
        expanded,
    ) { documentation, expanded ->
        createViewState(documentation, expanded)
    }
        .distinctUntilChanged()

    private fun createViewState(documentation: Documentation?, expanded: Set<String>): DydxRewardsFaqsView.ViewState {
        return DydxRewardsFaqsView.ViewState(
            localizer = localizer,
            title = DydxRewardsFaqsHeaderView.ViewState(
                localizer = localizer,
                title = localizer.localize("APP.HEADER.HELP"),
                learnMoreText = localizer.localize("APP.GENERAL.LEARN_MORE"),
                learnMoreAction = {
                    val url = abacusStateManager.environment?.links?.tradingRewardsLearnMore
                    if (url != null) {
                        router.navigateTo(url)
                    }
                },
            ),
            faqs = documentation?.tradingRewardsFAQs?.map {
                val question = localizer.localize(it.questionLocalizationKey)
                DydxRewardsFaqItemView.ViewState(
                    localizer = localizer,
                    question = question,
                    answer = localizer.localize(it.answerLocalizationKey),
                    expanded = expanded.contains(question),
                    tapped = { question ->
                        val modified = expanded.toMutableSet()
                        if (modified.contains(question)) {
                            modified.remove(question)
                        } else {
                            modified.add(question)
                        }
                        this.expanded.value = modified
                    },
                )
            } ?: listOf(),
        )
    }
}
