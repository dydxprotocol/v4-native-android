package exchange.dydx.trading.feature.trade.tradeinput

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.buttons.PlatformPillItem
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.tabgroups.PlatformTabGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton
import exchange.dydx.trading.feature.shared.views.LabeledTextInput

data class LeverageTextAndValue(val text: String, val value: String)

@Preview
@Composable
fun Preview_DydxTradeInputTargetLeverageView() {
    DydxThemedPreviewSurface {
        DydxTradeInputTargetLeverageView.Content(
            Modifier,
            DydxTradeInputTargetLeverageView.ViewState.preview,
        )
    }
}

object DydxTradeInputTargetLeverageView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val leverageText: String?,
        val leverageOptions: List<LeverageTextAndValue>?,
        val selectAction: ((String) -> Unit)? = null,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                leverageText = "1.0",
                leverageOptions = listOf(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputTargetLeverageViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .animateContentSize()
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_3),
        ) {
            NavigationHeader(
                modifier = Modifier,
                state = state,
            )
            PlatformDivider()
            Description(
                modifier = Modifier,
                state = state,
            )
            LeverageEditField(
                modifier = Modifier,
                state = state,
            )
            LeverageOptions(
                modifier = Modifier,
                state = state,
            )
            Spacer(modifier = Modifier.weight(1f))
            ActionButton(
                modifier = Modifier,
                state = state,
            )
        }
    }

    @Composable
    fun NavigationHeader(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                modifier = Modifier.padding(horizontal = 0.dp),
                style = TextStyle.dydxDefault
                    .themeFont(
                        fontSize = ThemeFont.FontSize.large,
                        fontType = ThemeFont.FontType.plus,
                    )
                    .themeColor(ThemeColor.SemanticColor.text_primary),
                text = state.localizer.localize("APP.TRADE.ADJUST_TARGET_LEVERAGE"),
            )
            Spacer(modifier = Modifier.weight(1f))
            HeaderViewCloseBotton(
                closeAction = state.closeAction,
            )
        }
    }

    @Composable
    fun Description(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                modifier = Modifier.padding(horizontal = 0.dp),
                text = state.localizer.localize("APP.TRADE.ADJUST_TARGET_LEVERAGE_DESCRIPTION"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )
        }
    }

    @Composable
    fun LeverageEditField(
        modifier: Modifier,
        state: ViewState?
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = 8.dp,
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InputFieldScaffold(modifier) {
                LabeledTextInput.Content(
                    modifier = Modifier,
                    state = LabeledTextInput.ViewState(
                        localizer = MockLocalizer(),
                        label = state?.localizer?.localize("APP.TRADE.TARGET_LEVERAGE"),
                        value = state?.leverageText ?: "",
                        onValueChanged = {
                            state?.selectAction?.invoke(
                                it,
                            )
                        },
                    ),
                )
            }
        }
    }

    @Composable
    fun LeverageOptions(
        modifier: Modifier,
        state: ViewState?
    ) {
        Row(
            modifier = modifier
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = 8.dp,
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlatformTabGroup(
                modifier = Modifier.fillMaxWidth(),
                scrollingEnabled = false,
                items = state?.leverageOptions?.map {
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
                                text = it.text,
                                modifier = Modifier,
                                style = TextStyle.dydxDefault
                                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                                    .themeFont(fontSize = ThemeFont.FontSize.small),

                            )
                        }
                    }
                } ?: listOf(),
                selectedItems = state?.leverageOptions?.map {
                    { modifier ->
                        PlatformPillItem(
                            modifier = modifier
                                .padding(
                                    vertical = 4.dp,
                                    horizontal = 8.dp,
                                ),
                            backgroundColor = ThemeColor.SemanticColor.layer_2,
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
                } ?: listOf(),
                equalWeight = false,
                currentSelection = state?.leverageOptions?.indexOfFirst {
                    it.value.toDouble() == state.leverageText?.toDouble()
                },
                onSelectionChanged = { it ->
                    state?.leverageOptions?.get(it)?.value?.let { value ->
                        state.selectAction?.invoke(value)
                    }
                },
            )
        }
    }

    @Composable
    fun ActionButton(
        modifier: Modifier,
        state: ViewState?
    ) {
        PlatformButton(
            modifier = Modifier
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                )
                .fillMaxWidth(),
            text = state?.localizer?.localize("APP.TRADE.CONFIRM_LEVERAGE"),
            state = PlatformButtonState.Primary,
        ) {
            state?.closeAction?.invoke()
        }
    }
}
