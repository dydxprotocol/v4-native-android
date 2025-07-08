package exchange.dydx.trading.feature.receipt.validation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxValidationViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxValidationView.ViewState?> =
        combine(
            abacusStateManager.state.validationErrors,
            abacusStateManager.state.transferInput,
        ) { validationErrors, transferInput ->
            createViewState(validationErrors, transferInput)
        }
            .distinctUntilChanged()

    private fun createViewState(
        validationErrors: List<ValidationError>?,
        transferInput: TransferInput?,
    ): DydxValidationView.ViewState {
        val transferError = transferInput?.errorMessage ?: transferInput?.errors
        val firstBlockingError = validationErrors?.firstOrNull { it.type == ErrorType.error }
        val firstWarning = validationErrors?.firstOrNull { it.type == ErrorType.warning }
        return DydxValidationView.ViewState(
            localizer = localizer,
            state = when {
                firstBlockingError != null -> DydxValidationView.State.Error
                firstWarning != null -> DydxValidationView.State.Warning
                transferError?.isNotEmpty() == true -> DydxValidationView.State.Error
                else -> DydxValidationView.State.None
            },
            title = when {
                firstBlockingError != null -> firstBlockingError.resources.title?.localizedString(localizer)
                firstWarning != null -> firstWarning.resources.title?.localizedString(localizer)
                transferError?.isNotEmpty() == true -> null
                else -> null
            },
            message = when {
                firstBlockingError != null -> firstBlockingError.resources.text?.localizedString(localizer)
                firstWarning != null -> firstWarning.resources.text?.localizedString(localizer)
                transferError?.isNotEmpty() == true -> transferError
                else -> null
            },
            link = when {
                firstBlockingError != null -> createLink(
                    text = firstBlockingError.linkText,
                    url = firstBlockingError.link,
                )
                firstWarning != null -> createLink(
                    text = firstWarning.linkText,
                    url = firstWarning.link,
                )
                else -> null
            },
        )
    }

    private fun createLink(
        text: String?,
        url: String?,
    ): DydxValidationView.Link? {
        if (text.isNullOrEmpty() || url.isNullOrEmpty()) {
            return null
        }

        return DydxValidationView.Link(
            text = localizer.localize(text),
            action = {
                router.navigateTo(url)
            },
        )
    }
}
