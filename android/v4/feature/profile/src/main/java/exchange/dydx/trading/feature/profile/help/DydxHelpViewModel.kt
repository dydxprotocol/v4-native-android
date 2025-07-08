package exchange.dydx.trading.feature.profile.help

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class DydxHelpViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxHelpView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxHelpView.ViewState {
        return DydxHelpView.ViewState(
            localizer = localizer,
            backAction = {
                router.navigateBack()
            },
            items = listOfNotNull(
                abacusStateManager.environment?.links?.help?.let {
                    DydxHelpItemView.ViewState(
                        localizer = localizer,
                        icon = R.drawable.help_chatbot,
                        title = localizer.localize("APP.HELP_MODAL.LIVE_CHAT"),
                        subtitle = localizer.localize("APP.HELP_MODAL.LIVE_CHAT_DESCRIPTION"),
                        onTapAction = {
                            router.navigateTo(it)
                        },
                    )
                },

                abacusStateManager.environment?.links?.community?.let {
                    DydxHelpItemView.ViewState(
                        localizer = localizer,
                        icon = R.drawable.help_discord,
                        title = localizer.localize("APP.HELP_MODAL.JOIN_DISCORD"),
                        subtitle = localizer.localize("APP.HELP_MODAL.JOIN_DISCORD_DESCRIPTION"),
                        onTapAction = {
                            router.navigateTo(it)
                        },
                    )
                },

                abacusStateManager.environment?.links?.feedback?.let {
                    DydxHelpItemView.ViewState(
                        localizer = localizer,
                        icon = R.drawable.help_feedback,
                        title = localizer.localize("APP.HELP_MODAL.PROVIDE_FEEDBACK"),
                        subtitle = localizer.localize("APP.HELP_MODAL.PROVIDE_FEEDBACK_DESCRIPTION"),
                        onTapAction = {
                            router.navigateTo(it)
                        },
                    )
                },

                abacusStateManager.environment?.links?.documentation?.let {
                    DydxHelpItemView.ViewState(
                        localizer = localizer,
                        icon = R.drawable.help_api,
                        title = localizer.localize("APP.HEADER.API_DOCUMENTATION"),
                        subtitle = localizer.localize("APP.HELP_MODAL.API_DOCUMENTATION_DESCRIPTION"),
                        onTapAction = {
                            router.navigateTo(it)
                        },
                    )
                },
            ),
        )
    }
}
