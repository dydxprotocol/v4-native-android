package exchange.dydx.trading.feature.trade.margin.components.type

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformPillItem
import exchange.dydx.platformui.components.tabgroups.PlatformTabGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.trade.margin.DydxAdjustMarginInputView.MarginDirection
@Preview
@Composable
fun Preview_DydxAdjustMarginInputTypeView() {
    DydxThemedPreviewSurface {
        DydxAdjustMarginInputTypeView.Content(
            Modifier,
            DydxAdjustMarginInputTypeView.ViewState.preview,
        )
    }
}

object DydxAdjustMarginInputTypeView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val direction: MarginDirection = MarginDirection.Add,
        val marginDirectionAction: ((direction: MarginDirection) -> Unit) = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAdjustMarginInputTypeViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val focusManager = LocalFocusManager.current

        val directions = listOf(MarginDirection.Add, MarginDirection.Remove)

        PlatformTabGroup(
            modifier = modifier
                .fillMaxWidth()
                .height(42.dp),
            scrollingEnabled = false,
            items = directions.map {
                { modifier ->
                    PlatformPillItem(
                        modifier = modifier
                            .padding(
                                vertical = 4.dp,
                                horizontal = 8.dp,
                            ),
                        backgroundColor = ThemeColor.SemanticColor.layer_5,
                    ) {
                        Text(
                            text = marginDirectionText(it, state.localizer),
                            modifier = Modifier,
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_tertiary)
                                .themeFont(fontSize = ThemeFont.FontSize.small),

                        )
                    }
                }
            },
            selectedItems = directions.map {
                { modifier ->
                    PlatformPillItem(
                        modifier = modifier
                            .padding(
                                vertical = 4.dp,
                                horizontal = 8.dp,
                            ),
                        backgroundColor = ThemeColor.SemanticColor.layer_2,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = marginDirectionText(it, state.localizer),
                                modifier = Modifier,
                                style = TextStyle.dydxDefault
                                    .themeColor(ThemeColor.SemanticColor.text_primary)
                                    .themeFont(fontSize = ThemeFont.FontSize.small),
                            )
                        }
                    }
                }
            },
            currentSelection = if (state.direction == MarginDirection.Add) 0 else 1,
            onSelectionChanged = { it ->
                focusManager.clearFocus()
                state.marginDirectionAction.invoke(directions[it])
            },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        )
    }

    private fun marginDirectionText(
        direction: MarginDirection,
        localizer: LocalizerProtocol
    ): String {
        return when (direction) {
            MarginDirection.Add -> localizer.localize("APP.TRADE.ADD_MARGIN")
            MarginDirection.Remove -> localizer.localize("APP.TRADE.REMOVE_MARGIN")
        }
    }
}
