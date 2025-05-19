package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.goodtil

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformAccessoryButton
import exchange.dydx.platformui.components.menus.PlatformDropdownMenu
import exchange.dydx.platformui.components.menus.PlatformMenuItem
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold
import exchange.dydx.trading.feature.shared.views.LabeledSelectionInput
import exchange.dydx.trading.feature.shared.views.LabeledTextInput

@Preview
@Composable
fun Preview_DydxTradeInputGoodTilView() {
    DydxThemedPreviewSurface {
        DydxTradeInputGoodTilView.Content(Modifier, DydxTradeInputGoodTilView.ViewState.preview)
    }
}

object DydxTradeInputGoodTilView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val labeledTextInput: LabeledTextInput.ViewState? = null,
        val labeledSelectionInput: LabeledSelectionInput.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                labeledTextInput = LabeledTextInput.ViewState.preview,
                labeledSelectionInput = LabeledSelectionInput.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputGoodTilViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val expanded: MutableState<Boolean> = remember {
            mutableStateOf(false)
        }

        InputFieldScaffold(modifier) {
            Row(
                verticalAlignment = Alignment.CenterVertically,

            ) {
                LabeledTextInput.Content(
                    modifier = Modifier.weight(1f),
                    state = state.labeledTextInput,
                )

                Box {
                    PlatformAccessoryButton(
                        modifier = Modifier,
                        action = { expanded.value = !expanded.value },
                    ) {
                        Text(
                            modifier = Modifier.padding(ThemeShapes.InputPaddingValues),
                            text = state.labeledSelectionInput?.options?.getOrNull(
                                state.labeledSelectionInput?.selectedIndex ?: 0,
                            ) ?: "",
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.small)
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    }

                    PlatformDropdownMenu(
                        expanded = expanded,
                        items = state.labeledSelectionInput?.options?.mapIndexed { index, option ->
                            PlatformMenuItem(
                                text = option,
                                onClick = {
                                    expanded.value = false
                                    state.labeledSelectionInput?.onSelectionChanged?.invoke(index)
                                },
                            )
                        } ?: listOf(),
                    )
                }
            }
        }
    }
}
