package exchange.dydx.trading.feature.vault.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

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
                                .padding(
                                    vertical = ThemeShapes.VerticalPadding,
                                ),
                            text = state.localizer.localize("APP.GENERAL.WITHDRAW"),
                            state = PlatformButtonState.Secondary,
                        ) {
                            state.withdrawAction.invoke()
                        }
                    }
                    ButtonType.DEPOSIT -> {
                        PlatformButton(
                            modifier = Modifier.weight(1f)
                                .padding(
                                    vertical = ThemeShapes.VerticalPadding,
                                ),
                            text = state.localizer.localize("APP.GENERAL.DEPOSIT"),
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

