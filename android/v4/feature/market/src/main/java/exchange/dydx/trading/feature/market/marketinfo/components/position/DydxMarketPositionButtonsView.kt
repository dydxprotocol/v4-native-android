package exchange.dydx.trading.feature.market.marketinfo.components.position

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.scaffolds.InputFieldScaffold
import exchange.dydx.utilities.utils.toDp

@Preview
@Composable
fun Preview_DydxMarketPositionButtonsView() {
    DydxThemedPreviewSurface {
        DydxMarketPositionButtonsView.Content(
            Modifier,
            DydxMarketPositionButtonsView.ViewState.preview,
        )
    }
}

object DydxMarketPositionButtonsView : DydxComponent {

    data class TriggerViewState(
        val label: String? = null,
        val triggerPrice: String? = null,
        val limitPrice: String? = null,
        val sizePercent: String? = null,
        val hasMultipleOrders: Boolean = false,
    ) {
        companion object {
            val preview = TriggerViewState(
                label = "TP",
                triggerPrice = "$120.0",
                limitPrice = "$110.0",
                sizePercent = "10%",
                hasMultipleOrders = false,
            )
        }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val addTriggerAction: (() -> Unit)? = null,
        val closeAction: (() -> Unit)? = null,
        val editMarginAction: (() -> Unit)? = null,
        val takeProfitTrigger: TriggerViewState? = null,
        val stopLossTrigger: TriggerViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                takeProfitTrigger = TriggerViewState.preview,
                stopLossTrigger = TriggerViewState.preview,
                editMarginAction = {},
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketPositionButtonsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            var size by remember { mutableStateOf(IntSize.Zero) }

            if (state.takeProfitTrigger == null && state.stopLossTrigger == null) {
                if (state.editMarginAction == null) {
                    AddTpSlButton(
                        modifier = Modifier,
                        state = state,
                    )

                    Spacer(modifier = Modifier.width(ThemeShapes.HorizontalPadding))

                    ClosePositionButton(
                        modifier = Modifier,
                        state = state,
                    )
                } else {
                    Column {
                        Row {
                            AddTpSlButton(
                                modifier = Modifier,
                                state = state,
                            )

                            Spacer(modifier = Modifier.width(ThemeShapes.HorizontalPadding))

                            EditMarginButton(
                                modifier = Modifier,
                                state = state,
                            )
                        }

                        Row {
                            ClosePositionButton(
                                modifier = Modifier,
                                state = state,
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
                    ) {
                        state.takeProfitTrigger?.let {
                            TriggerViewContent(
                                modifier = Modifier
                                    .weight(1f)
                                    .onSizeChanged { size = it },
                                state = it,
                                localizer = state.localizer,
                                action = state.addTriggerAction ?: {},
                            )
                        } ?: PlatformButton(
                            text = state.localizer.localize("APP.TRADE.ADD_TAKE_PROFIT"),
                            state = PlatformButtonState.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(size.height.toDp),
                            action = state.addTriggerAction ?: {},
                        )

                        state.stopLossTrigger?.let {
                            TriggerViewContent(
                                modifier = Modifier
                                    .weight(1f)
                                    .onSizeChanged { size = it },
                                state = it,
                                localizer = state.localizer,
                                action = state.addTriggerAction ?: {},
                            )
                        } ?: PlatformButton(
                            text = state.localizer.localize("APP.TRADE.ADD_STOP_LOSS"),
                            state = PlatformButtonState.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(size.height.toDp),
                            action = state.addTriggerAction ?: {},
                        )
                    }

                    Row {
                        ClosePositionButton(
                            modifier = Modifier,
                            state = state,
                        )

                        if (state.editMarginAction != null) {
                            Spacer(modifier = Modifier.width(ThemeShapes.HorizontalPadding))

                            EditMarginButton(
                                modifier = Modifier,
                                state = state,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.AddTpSlButton(
        modifier: Modifier,
        state: ViewState,
    ) {
        PlatformButton(
            text = state.localizer.localize("APP.TRADE.ADD_TP_SL"),
            state = PlatformButtonState.Secondary,
            modifier = modifier
                .padding(vertical = ThemeShapes.VerticalPadding)
                .weight(1f),
            action = state.addTriggerAction ?: {},
        )
    }

    @Composable
    private fun RowScope.ClosePositionButton(
        modifier: Modifier,
        state: ViewState,
    ) {
        PlatformButton(
            text = state.localizer.localize("APP.TRADE.CLOSE_POSITION"),
            state = PlatformButtonState.Destructive,
            modifier = modifier
                .padding(vertical = ThemeShapes.VerticalPadding)
                .weight(1f),
            action = state.closeAction ?: {},
        )
    }

    @Composable
    private fun RowScope.EditMarginButton(
        modifier: Modifier,
        state: ViewState,
    ) {
        PlatformButton(
            text = state.localizer.localize("APP.TRADE.EDIT_MARGIN"),
            state = PlatformButtonState.Secondary,
            modifier = modifier
                .padding(vertical = ThemeShapes.VerticalPadding)
                .weight(1f),
            action = state.editMarginAction ?: {},
            leadingContent = {
                Icon(
                    painter = painterResource(id = exchange.dydx.trading.feature.shared.R.drawable.icon_edit),
                    contentDescription = "",
                    tint = ThemeColor.SemanticColor.text_secondary.color,
                    modifier = Modifier.size(20.dp),
                )
            },
        )
    }

    @Composable
    private fun TriggerViewContent(
        modifier: Modifier,
        state: TriggerViewState,
        localizer: LocalizerProtocol,
        action: () -> Unit = {}
    ) {
        InputFieldScaffold(
            modifier = modifier,
        ) {
            Column(
                modifier = Modifier
                    .clickable { action() }
                    .padding(vertical = ThemeShapes.VerticalPadding),
                verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
            ) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .padding(horizontal = ThemeShapes.HorizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = state.label ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.tiny)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (state.hasMultipleOrders) {
                        Text(
                            text = localizer.localize("APP.GENERAL.MULTIPLE"),
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.medium)
                                .themeColor(ThemeColor.SemanticColor.text_secondary),
                        )
                    } else {
                        Text(
                            modifier = Modifier,
                            text = state.triggerPrice ?: "",
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.medium)
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    }
                }

                if (!state.hasMultipleOrders && (state.limitPrice != null || state.sizePercent != null)) {
                    PlatformDivider()

                    if (state.limitPrice != null) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = ThemeShapes.HorizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = localizer.localize("APP.TRADE.LIMIT_ORDER_SHORT"),
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.tiny)
                                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                                maxLines = 1,
                                modifier = Modifier.weight(1f),
                            )

                            Text(
                                text = state.limitPrice,
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                                    .themeColor(ThemeColor.SemanticColor.text_secondary),
                            )
                        }
                    }

                    if (state.sizePercent != null) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = ThemeShapes.HorizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = localizer.localize("APP.GENERAL.AMOUNT"),
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.tiny)
                                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                                maxLines = 1,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = state.sizePercent,
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                                    .themeColor(ThemeColor.SemanticColor.text_secondary),
                            )
                        }
                    }
                }
            }
        }
    }
}
