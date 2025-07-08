package exchange.dydx.trading.feature.vault.depositwithdraw

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.feature.receipt.validation.DydxValidationView

internal fun ValidationError.createViewModel(
    localizer: LocalizerProtocol
): DydxValidationView.ViewState {
    return DydxValidationView.ViewState(
        localizer = localizer,
        state = when (this.type) {
            ErrorType.error -> DydxValidationView.State.Error
            ErrorType.warning -> DydxValidationView.State.Warning
            ErrorType.required -> DydxValidationView.State.None
        },
        title = this.resources.title?.localized ?: this.resources.title?.stringKey,
        message = this.resources.text?.localized ?: this.resources.text?.stringKey,
    )
}
