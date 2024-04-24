package exchange.dydx.trading.feature.trade

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.platformui.components.inputs.PlatformInputAlertState

val ValidationError.alertState: PlatformInputAlertState
    get() = when (type) {
        ErrorType.error -> PlatformInputAlertState.Error
        ErrorType.warning -> PlatformInputAlertState.Warning
        else -> PlatformInputAlertState.None
    }
