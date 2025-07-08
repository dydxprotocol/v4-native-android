package exchange.dydx.trading.feature.shared.apprating

import android.R.attr.action
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun AppRatingDialogScaffold(
    dialog: AppRatingDialog,
) {
    val openAlertDialog = dialog.showing.collectAsState().value
    if (openAlertDialog) {
        Dialog(
            onDismissRequest = {
                dialog.showing.value = false
                dialog.onDismiss.invoke()
            },
        ) {
            Column(
                modifier = Modifier
                    .themeColor(background = ThemeColor.SemanticColor.layer_1)
                    .clip(RoundedCornerShape(24.dp))
                    .padding(16.dp),
            ) {
                Text(
                    text = dialog.localizer.localize("RATE_APP.QUESTION"),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.medium,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    PlatformButton(
                        action = {
                            dialog.showing.value = false
                            dialog.onPositiveClick.invoke()
                        },
                    ) {
                        Text(
                            text = dialog.localizer.localize("RATE_APP.YES"),
                            style = TextStyle.dydxDefault
                                .themeFont(
                                    fontType = ThemeFont.FontType.plus,
                                    fontSize = ThemeFont.FontSize.small,
                                )
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    }

                    PlatformButton(
                        action = {
                            dialog.showing.value = false
                            dialog.onNegativeClick.invoke()
                        },
                    ) {
                        Text(
                            text = dialog.localizer.localize("RATE_APP.NO"),
                            style = TextStyle.dydxDefault
                                .themeFont(
                                    fontType = ThemeFont.FontType.plus,
                                    fontSize = ThemeFont.FontSize.small,
                                )
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    }
                }
            }
        }
    }
}

data class AppRatingDialog(
    val localizer: LocalizerProtocol,
    val onDismiss: () -> Unit = {},
    val onNegativeClick: () -> Unit = {},
    val onPositiveClick: () -> Unit = {},
    val showing: MutableStateFlow<Boolean> = MutableStateFlow(false),
)
