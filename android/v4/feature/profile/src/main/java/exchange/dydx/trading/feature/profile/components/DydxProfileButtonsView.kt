package exchange.dydx.trading.feature.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.PlatformDialog
import exchange.dydx.platformui.components.PlatformDialogScaffold
import exchange.dydx.platformui.components.buttons.PlatformIconButton
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxProfileButtonsView() {
    DydxThemedPreviewSurface {
        DydxProfileButtonsView.Content(Modifier, DydxProfileButtonsView.ViewState.preview)
    }
}

object DydxProfileButtonsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val settingsAction: (() -> Unit)? = null,
        val helpAction: (() -> Unit)? = null,
        val walletAction: (() -> Unit)? = null,
        val signOutAction: (() -> Unit)? = null,
        val onboardAction: (() -> Unit)? = null,
        val walletImageUrl: String? = null,
        val onboarded: Boolean = false,
        val platformDialog: PlatformDialog,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                platformDialog = PlatformDialog(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxProfileButtonsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            createButton(
                icon = R.drawable.icon_settings,
                title = state.localizer.localize("APP.EMAIL_NOTIFICATIONS.SETTINGS"),
                action = state.settingsAction,
                modifier = Modifier.weight(1f),
            )

            createButton(
                icon = R.drawable.icon_tutorial,
                title = state.localizer.localize("APP.HEADER.HELP"),
                action = state.helpAction,
                modifier = Modifier.weight(1f),
            )

            createButton(
                icon = state.walletImageUrl ?: R.drawable.icon_wallet,
                title = state.localizer.localize("APP.GENERAL.ACCOUNT"),
                action = state.walletAction,
                tint = if (state.walletImageUrl != null) null else ThemeColor.SemanticColor.text_secondary,
                modifier = Modifier.weight(1f),
            )

            if (state.onboarded) {
                createButton(
                    icon = R.drawable.settings_signout,
                    title = state.localizer.localize("APP.GENERAL.SIGN_OUT"),
                    tint = null,
                    action = state.signOutAction,
                    modifier = Modifier.weight(1f),
                )
            } else {
                createButton(
                    icon = R.drawable.icon_wallet_connect,
                    title = state.localizer.localize("APP.GENERAL.CONNECT"),
                    action = state.onboardAction,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        PlatformDialogScaffold(dialog = state.platformDialog)
    }

    @Composable
    private fun createButton(
        modifier: Modifier,
        icon: Any,
        title: String,
        tint: ThemeColor.SemanticColor? = ThemeColor.SemanticColor.text_secondary,
        action: (() -> Unit)?,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier,
        ) {
            PlatformIconButton(action = action ?: {}) {
                PlatformImage(
                    icon = icon,
                    modifier = Modifier.size(24.dp),
                    colorFilter = if (tint != null) ColorFilter.tint(tint.color) else null,
                )
            }

            Text(
                text = title,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
