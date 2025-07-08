package exchange.dydx.trading.feature.transfer.search

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.views.TokenTextView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxTransferSearchViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private val paramFlow: Flow<DydxTransferSearchParam?>,
) : ViewModel(), DydxViewModel {

    private val searchTextFlow: MutableStateFlow<String?> = MutableStateFlow(null)

    val state: Flow<DydxTransferSearchView.ViewState?> =
        combine(
            paramFlow,
            searchTextFlow,
        ) { param, searchText ->
            createViewState(param, searchText)
        }
            .distinctUntilChanged()

    private fun createViewState(
        param: DydxTransferSearchParam?,
        searchText: String?,
    ): DydxTransferSearchView.ViewState {
        fun getToken(tokenType: String?): TokenTextView.ViewState? {
            return tokenType?.let {
                param?.resources?.tokenResources?.get(it)?.symbol?.let {
                    TokenTextView.ViewState(symbol = it)
                }
            }
        }

        return DydxTransferSearchView.ViewState(
            localizer = localizer,
            closeAction = {
                router.navigateBack()
            },
            items = param?.options?.filter { option ->
                val text = option.localizedString(localizer) ?: return@filter false
                if (searchText.isNullOrEmpty()) return@filter true

                return@filter text.contains(searchText, ignoreCase = true)
            }?.map { option ->
                DydxTransferSearchItem.ViewState(
                    localizer = localizer,
                    id = option.type,
                    text = option.localizedString(localizer),
                    icon = option.iconUrl,
                    tokenText = getToken(option.type),
                    isSelected = option == param.selected,
                )
            } ?: emptyList(),
            itemSelected = { id ->
                param?.selectedCallback?.invoke(
                    param.options?.firstOrNull { it.type == id }
                        ?: return@ViewState,
                )
                router.navigateBack()
            },
            searchText = searchText ?: "",
            searchTextChanged = { text ->
                searchTextFlow.value = text
            },
        )
    }
}
