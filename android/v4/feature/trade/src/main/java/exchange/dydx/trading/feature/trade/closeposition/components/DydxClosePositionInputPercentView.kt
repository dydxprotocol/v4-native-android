package exchange.dydx.trading.feature.trade.closeposition.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformSelectionButton
import exchange.dydx.platformui.components.tabgroups.PlatformTabGroup
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxClosePositionInputPercentView() {
    DydxThemedPreviewSurface {
        DydxClosePositionInputPercentView.Content(
            Modifier,
            DydxClosePositionInputPercentView.ViewState.preview,
        )
    }
}

object DydxClosePositionInputPercentView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val options: List<String> = listOf(),
        val selectedIndex: Int? = null,
        val onSelectionChanged: (Int) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                options = listOf("25%", "50%", "75%", "100%"),
                selectedIndex = 0,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxClosePositionInputPercentViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val firstLine = state.options.subList(0, 3)
        val secondLine = state.options.subList(3, 4)
        Column(
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            Row {
                Spacer(modifier = Modifier.weight(1f))
                PlatformTabGroup(
                    //   horizontalArrangement = Arrangement.spacedBy(4.dp),
                    scrollingEnabled = false,
                    //   equalWeight = true,
                    items = firstLine.mapIndexed { index, option ->
                        { modifier ->
                            Item(
                                modifier = modifier,
                                option = option,
                                selected = false,
                            )
                        }
                    },
                    selectedItems = firstLine.mapIndexed { index, option ->
                        { modifier ->
                            Item(
                                modifier = modifier,
                                option = option,
                                selected = true,
                            )
                        }
                    },
                    currentSelection = state.selectedIndex?.let { index ->
                        if (index < 3) index else null
                    },
                    onSelectionChanged = { index ->
                        state.onSelectionChanged(index)
                    },
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Row {
                Spacer(modifier = Modifier.weight(1f))
                PlatformTabGroup(
                    modifier = Modifier,
                    scrollingEnabled = false,
                    items = secondLine.mapIndexed { index, option ->
                        { modifier ->
                            Item(
                                modifier = modifier,
                                option = option,
                                selected = false,
                            )
                        }
                    },
                    selectedItems = secondLine.mapIndexed { index, option ->
                        { modifier ->
                            Item(
                                modifier = modifier,
                                option = option,
                                selected = true,
                            )
                        }
                    },
                    currentSelection = state.selectedIndex?.let { index ->
                        if (index >= 3) index - 3 else null
                    },
                    onSelectionChanged = { index ->
                        state.onSelectionChanged(index + 3)
                    },
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    @Composable
    fun Item(modifier: Modifier, option: String, selected: Boolean) {
        PlatformSelectionButton(
            modifier = modifier,
            selected = selected,
        ) {
            Text(
                text = option,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini),
            )
        }
    }
}
