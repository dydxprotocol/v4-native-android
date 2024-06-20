package exchange.dydx.dydxstatemanager

import exchange.dydx.abacus.output.input.ErrorString
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.utils.toJsonPrettyPrint

fun SelectionOption.localizedString(localizer: LocalizerProtocol): String? {
    val string = string
    val stringKey = stringKey
    if (string != null) {
        return string
    }
    if (stringKey != null) {
        return localizer.localize(stringKey)
    }
    return null
}

fun ErrorString.localizedString(localizer: LocalizerProtocol): String =
    localized ?: localizer.localize(stringKey)

fun TradeInput.selectedTypeText(localizer: LocalizerProtocol): String? {
    val selectedType = options?.typeOptions?.firstOrNull { it.type == type?.rawValue }
    return selectedType?.localizedString(localizer)
}

fun ParsingError.localizedString(localizer: LocalizerProtocol): String? {
    val stringKey = stringKey
    return if (stringKey != null) localizer.localize(stringKey) else null
}

fun LocalizerProtocol.localizeWithParams(path: String, params: Map<String, String>): String {
    return localize(path = path, paramsAsJson = params.toJsonPrettyPrint())
}

val OrderStatus.canCancel: Boolean
    get() = when (this) {
        OrderStatus.Open, OrderStatus.Pending, OrderStatus.PartiallyFilled, OrderStatus.Untriggered -> true
        else -> false
    }

fun MarginMode.localizedString(localizer: LocalizerProtocol): String? {
    when (this) {
        MarginMode.Cross -> return localizer.localize("APP.GENERAL.CROSS")
        MarginMode.Isolated -> return localizer.localize("APP.GENERAL.ISOLATED")
    }
}
