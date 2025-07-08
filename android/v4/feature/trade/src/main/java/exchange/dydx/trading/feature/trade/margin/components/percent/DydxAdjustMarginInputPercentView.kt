package exchange.dydx.trading.feature.trade.margin.components.percent

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformSelectionButton
import exchange.dydx.platformui.components.tabgroups.PlatformTabGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.trade.margin.DydxAdjustMarginInputView

@Preview
@Composable
fun Preview_DydxAdjustMarginInputPercentView() {
    DydxThemedPreviewSurface {
        DydxAdjustMarginInputPercentView.Content(
            Modifier,
            DydxAdjustMarginInputPercentView.ViewState.preview,
        )
    }
}

object DydxAdjustMarginInputPercentView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val percentage: Double?,
        val percentageOptions: List<DydxAdjustMarginInputView.PercentageOption>,
        val onPercentageChanged: ((percentage: Double) -> Unit) = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                percentage = 0.1,
                percentageOptions = listOf(
                    DydxAdjustMarginInputView.PercentageOption("10%", 0.1),
                    DydxAdjustMarginInputView.PercentageOption("20%", 0.2),
                    DydxAdjustMarginInputView.PercentageOption("30%", 0.3),
                    DydxAdjustMarginInputView.PercentageOption("50%", 0.5),
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAdjustMarginInputPercentViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val focusManager = LocalFocusManager.current

        PlatformTabGroup(
            modifier = modifier.fillMaxWidth(),
            items = state.percentageOptions.map {
                { modifier ->
                    PlatformSelectionButton(
                        modifier = modifier.size(width = 60.dp, height = 40.dp),
                        selected = false,
                    ) {
                        Text(
                            text = it.text,
                            modifier = Modifier,
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_tertiary)
                                .themeFont(fontSize = ThemeFont.FontSize.small),

                        )
                    }
                }
            },
            selectedItems = state.percentageOptions.map {
                { modifier ->
                    PlatformSelectionButton(
                        modifier = modifier.size(width = 60.dp, height = 40.dp),
                        selected = true,
                    ) {
                        Text(
                            text = it.text,
                            modifier = Modifier,
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_primary)
                                .themeFont(fontSize = ThemeFont.FontSize.small),

                        )
                    }
                }
            },
            equalWeight = false,
            currentSelection = state.percentageOptions.indexOfFirst {
                it.percentage == state.percentage
            },
            onSelectionChanged = {
                focusManager.clearFocus()
                state.onPercentageChanged(state.percentageOptions[it].percentage)
            },
        )
    }
}
