package exchange.dydx.trading.feature.trade.tradeinput.components.inputfields.ordertype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Preview
@Composable
fun Preview_DydxTradeInputOrderTypeView() {
    DydxThemedPreviewSurface {
        DydxTradeInputOrderTypeView.Content(Modifier, DydxTradeInputOrderTypeView.ViewState.preview)
    }
}

object DydxTradeInputOrderTypeView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val tokenLogoUrl: String? = null,
        val orderTypes: List<String> = listOf(),
        val selectedIndex: Int = 0,
        val onSelectionChanged: (Int) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                orderTypes = listOf("Limit", "Market", "Stop", "Stop Limit", "Trailing Stop"),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputOrderTypeViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        PlatformTabGroup(
            modifier = modifier,
            items = state.orderTypes.map {
                { modifier ->
                    PlatformPillItem(
                        modifier = modifier,
                        backgroundColor = ThemeColor.SemanticColor.layer_5,
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier,
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_tertiary)
                                .themeFont(fontSize = ThemeFont.FontSize.small),

                        )
                    }
                }
            },
            selectedItems = state.orderTypes.map {
                { modifier ->
                    PlatformPillItem(
                        modifier = modifier,
                        backgroundColor = ThemeColor.SemanticColor.layer_2,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier,
                                style = TextStyle.dydxDefault
                                    .themeColor(ThemeColor.SemanticColor.text_primary)
                                    .themeFont(fontSize = ThemeFont.FontSize.small),
                            )
                        }
                    }
                }
            },
            currentSelection = state.selectedIndex,
            onSelectionChanged = state.onSelectionChanged,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        )
    }
}
