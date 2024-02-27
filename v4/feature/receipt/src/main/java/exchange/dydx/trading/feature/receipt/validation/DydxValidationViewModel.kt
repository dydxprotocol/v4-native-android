package exchange.dydx.trading.feature.receipt.validation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxValidationViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxValidationView.ViewState?> =
        abacusStateManager.state.validationErrors
            .map {
                createViewState(it)
            }
            .distinctUntilChanged()

    private fun createViewState(
        validationErrors: List<ValidationError>?,
    ): DydxValidationView.ViewState {
        val firstBlockingError = validationErrors?.firstOrNull { it.type == ErrorType.error }
        val firstWarning = validationErrors?.firstOrNull { it.type == ErrorType.warning }
        return DydxValidationView.ViewState(
            localizer = localizer,
            state = when {
                firstBlockingError != null -> DydxValidationView.State.Error
                firstWarning != null -> DydxValidationView.State.Warning
                else -> DydxValidationView.State.None
            },
            message = when {
                firstBlockingError != null -> firstBlockingError.resources.text?.localizedString(
                    localizer,
                )

                firstWarning != null -> firstWarning.resources.text?.localizedString(localizer)
                else -> null
            },
        )
    }
}
