package exchange.dydx.platformui.components.inputs

import exchange.dydx.platformui.designSystem.theme.ThemeColor

enum class PlatformInputAlertState {
    None,
    Error,
    Warning;

    val borderColor: ThemeColor.SemanticColor
        get() = when (this) {
            None -> ThemeColor.SemanticColor.layer_6
            Error -> ThemeColor.SemanticColor.color_red
            Warning -> ThemeColor.SemanticColor.color_yellow
        }

    val textColor: ThemeColor.SemanticColor
        get() = when (this) {
            None -> ThemeColor.SemanticColor.text_primary
            Error -> ThemeColor.SemanticColor.color_red
            Warning -> ThemeColor.SemanticColor.color_yellow
        }
}
