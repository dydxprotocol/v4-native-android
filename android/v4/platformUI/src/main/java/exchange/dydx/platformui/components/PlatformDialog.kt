package exchange.dydx.platformui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun PlatformDialogScaffold(
    dialog: PlatformDialog,
) {
    val openAlertDialog = dialog.showing.collectAsState().value

    val textEntry = remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    val icon = dialog.icon
    if (openAlertDialog) {
        AlertDialog(
            containerColor = ThemeColor.SemanticColor.layer_1.color,
            icon = if (icon != null) {
                {
                    Icon(icon, contentDescription = "")
                }
            } else {
                null
            },
            title = {
                if (dialog.title.isNotEmpty()) {
                    Text(
                        text = dialog.title,
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                }
            },
            text = {
                Column {
                    if (dialog.message.isNotEmpty()) {
                        Text(
                            text = dialog.message,
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.small)
                                .themeColor(ThemeColor.SemanticColor.text_secondary),
                        )
                    }

                    if (dialog.type == PlatformDialogType.TextEntry) {
                        TextField(
                            modifier = Modifier.focusRequester(focusRequester),
                            value = textEntry.value,
                            maxLines = 1,
                            textStyle = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.small)
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = ThemeColor.SemanticColor.layer_4.color,
                            ),
                            onValueChange = {
                                textEntry.value = it
                            },
                        )
                    }
                }
            },
            onDismissRequest = {
                dialog.cancelAction.invoke()
            },
            confirmButton = {
                PlatformButton(
                    action = {
                        dialog.showing.value = false
                        dialog.confirmAction.invoke(textEntry.value)
                    },
                ) {
                    Text(
                        text = dialog.confirmTitle ?: "Confirm",
                        style = TextStyle.dydxDefault
                            .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_secondary),
                    )
                }
            },
            dismissButton = {
                PlatformButton(
                    action = {
                        dialog.showing.value = false
                        dialog.cancelAction.invoke()
                    },
                ) {
                    Text(
                        text = dialog.cancelTitle ?: "Dismiss",
                        style = TextStyle.dydxDefault
                            .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_secondary),
                    )
                }
            },
        )

        if (dialog.type == PlatformDialogType.TextEntry) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}

enum class PlatformDialogType {
    TextEntry, Message
}

data class PlatformDialog(
    internal var type: PlatformDialogType = PlatformDialogType.Message,
    internal var title: String = "",
    internal var message: String = "",
    internal var cancelTitle: String? = null,
    internal var confirmTitle: String? = null,
    internal var icon: ImageVector? = null,
    internal var cancelAction: () -> Unit = {},
    internal var confirmAction: (String?) -> Unit = {},
    internal val showing: MutableStateFlow<Boolean> = MutableStateFlow(false),
) {
    fun showMessage(
        title: String = "",
        message: String = "",
        cancelTitle: String? = null,
        confirmTitle: String? = null,
        icon: ImageVector? = null,
        cancelAction: () -> Unit = {},
        confirmAction: () -> Unit = {},
    ) {
        this.type = PlatformDialogType.Message
        this.title = title
        this.message = message
        this.cancelTitle = cancelTitle
        this.confirmTitle = confirmTitle
        this.icon = icon
        this.cancelAction = cancelAction
        this.confirmAction = {
            confirmAction.invoke()
        }

        showing.value = true
    }

    fun showTextEntry(
        title: String = "",
        message: String = "",
        cancelTitle: String? = null,
        confirmTitle: String? = null,
        icon: ImageVector? = null,
        cancelAction: () -> Unit = {},
        confirmAction: (String?) -> Unit = {},
    ) {
        this.type = PlatformDialogType.TextEntry
        this.title = title
        this.message = message
        this.cancelTitle = cancelTitle
        this.confirmTitle = confirmTitle
        this.icon = icon
        this.cancelAction = cancelAction
        this.confirmAction = confirmAction

        showing.value = true
    }
}
