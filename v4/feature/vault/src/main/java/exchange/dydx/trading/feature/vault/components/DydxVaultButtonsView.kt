package exchange.dydx.trading.feature.vault.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxVaultButtonsView() {
    DydxThemedPreviewSurface {
        DydxVaultButtonsView.Content(Modifier, DydxVaultButtonsView.ViewState.preview)
    }
}

object DydxVaultButtonsView : DydxComponent {
    enum class ButtonType {
        DEPOSIT,
        WITHDRAW,
    }
    data class ViewState(
        val localizer: LocalizerProtocol,
        val depositAction: () -> Unit = {},
        val withdrawAction: () -> Unit = {},
        val buttons: List<ButtonType> = listOf(ButtonType.WITHDRAW, ButtonType.DEPOSIT),
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultButtonsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null || state.buttons.isEmpty()) {
            return
        }

        Row(
            modifier = modifier.padding(
                horizontal = ThemeShapes.HorizontalPadding,
            ).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            state.buttons.forEach { buttonType ->
                when (buttonType) {
                    ButtonType.WITHDRAW -> {
                        PlatformButton(
                            modifier = Modifier.weight(1f)
                                .size(44.dp),
                            text = state.localizer.localize("APP.GENERAL.WITHDRAW"),
                            leadingContent = {
                                PlatformImage(
                                    modifier = Modifier.size(20.dp),
                                    icon = R.drawable.icon_transfer_withdrawal,
                                    colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_primary.color),
                                )
                            },
                            state = PlatformButtonState.Secondary,
                        ) {
                            state.withdrawAction.invoke()
                        }
                    }
                    ButtonType.DEPOSIT -> {
                        PlatformButton(
                            modifier = Modifier.weight(1f)
                                .size(44.dp),
                            text = state.localizer.localize("APP.GENERAL.DEPOSIT"),
                            leadingContent = {
                                PlatformImage(
                                    modifier = Modifier.size(20.dp),
                                    icon = R.drawable.icon_transfer_deposit,
                                    colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.color_white.color),
                                )
                            },
                            state = PlatformButtonState.Primary,
                        ) {
                            state.depositAction.invoke()
                        }
                    }
                }
            }
        }
    }
}
