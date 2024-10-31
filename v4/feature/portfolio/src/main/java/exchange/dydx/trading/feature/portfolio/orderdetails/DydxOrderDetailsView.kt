package exchange.dydx.trading.feature.portfolio.orderdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
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
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton
import exchange.dydx.trading.feature.shared.views.SideTextView
import java.util.UUID

@Preview
@Composable
fun Preview_DydxOrderDetailsView() {
    DydxThemedPreviewSurface {
        DydxOrderDetailsView.Content(Modifier, DydxOrderDetailsView.ViewState.preview)
    }
}

object DydxOrderDetailsView : DydxComponent {

    data class Item(
        val title: String? = null,
        val value: ItemValue? = null,
        val id: UUID = UUID.randomUUID(),
    ) {
        sealed class ItemValue {
            data class Number(val value: String?) : ItemValue()
            data class StringValue(val value: String?) : ItemValue()
            object Checkmark : ItemValue()
            data class Any(val view: (@Composable () -> Unit)?) : ItemValue()
        }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val side: SideTextView.ViewState? = null,
        val logoUrl: String? = null,
        val items: List<Item> = emptyList(),
        val closeAction: (() -> Unit)? = null,
        val cancelAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                side = SideTextView.ViewState.preview,
                logoUrl = "https://media.dydx.exchange/currencies/eth.png",
                items = listOf(
                    Item(title = "Type", value = Item.ItemValue.StringValue("Market Order")),
                    Item(title = "Size", value = Item.ItemValue.Number("0.017 ETH")),
                    Item(title = "Date", value = Item.ItemValue.StringValue("2021-05-05 12:00:00")),
                    Item(title = "Price", value = Item.ItemValue.Number("$1,203.8")),
                    Item(title = "Fee", value = Item.ItemValue.Number("$0.0")),
                    Item(title = "Fee Liquidity", value = Item.ItemValue.StringValue("Taker")),
                ),
                cancelAction = {},
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxOrderDetailsViewModel = hiltViewModel()

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
                .background(ThemeColor.SemanticColor.layer_2.color)
                .fillMaxSize(),
        ) {
            HeaderView(
                modifier = Modifier,
                state = state,
            )

            PlatformDivider()

            ItemsView(
                modifier = Modifier,
                state = state,
            )

            Spacer(modifier = Modifier.weight(1f))

            if (state.cancelAction != null) {
                PlatformButton(
                    modifier = Modifier.fillMaxWidth()
                        .padding(
                            vertical = ThemeShapes.VerticalPadding,
                            horizontal = ThemeShapes.HorizontalPadding,
                        ),
                    text = state.localizer.localize("APP.TRADE.CANCEL_ORDER"),
                    state = PlatformButtonState.Destructive,
                ) {
                    state.cancelAction.invoke()
                }
            }
        }
    }

    @Composable
    private fun ItemsView(
        modifier: Modifier = Modifier,
        state: ViewState,
    ) {
        val listState = rememberLazyListState()

        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    vertical = ThemeShapes.VerticalPadding,
                ),
            state = listState,
        ) {
            items(items = state.items, key = { it.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .padding(horizontal = ThemeShapes.HorizontalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
                ) {
                    Text(
                        text = item.title ?: "",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    when (val value = item.value) {
                        is Item.ItemValue.Number -> {
                            Text(
                                text = value.value ?: "-",
                                style = TextStyle.dydxDefault
                                    .themeColor(ThemeColor.SemanticColor.text_primary)
                                    .themeFont(fontType = ThemeFont.FontType.number),
                            )
                        }
                        is Item.ItemValue.StringValue -> {
                            Text(
                                text = value.value ?: "-",
                                style = TextStyle.dydxDefault
                                    .themeColor(ThemeColor.SemanticColor.text_primary),
                                textAlign = TextAlign.End,
                            )
                        }
                        is Item.ItemValue.Checkmark -> {
                            PlatformImage(
                                modifier = Modifier,
                                icon = exchange.dydx.platformui.R.drawable.icon_check,
                                colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_primary.color),
                            )
                        }
                        is Item.ItemValue.Any -> {
                            value.view?.invoke()
                        }
                        else -> {
                            Text(
                                text = "-",
                                style = TextStyle.dydxDefault
                                    .themeColor(ThemeColor.SemanticColor.text_primary),
                            )
                        }
                    }
                }

                if (item != state.items.last()) {
                    PlatformDivider()
                }
            }
        }
    }

    @Composable
    private fun HeaderView(
        modifier: Modifier = Modifier,
        state: ViewState,
    ) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(
                    vertical = ThemeShapes.VerticalPadding,
                )
                .padding(top = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))

            PlatformRoundImage(
                icon = state.logoUrl,
                size = 40.dp,
            )

            Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))

            SideTextView.Content(
                modifier = Modifier,
                state = state.side,
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.extra, fontType = ThemeFont.FontType.plus),
            )

            Spacer(modifier = Modifier.weight(1f))

            HeaderViewCloseBotton(closeAction = state.closeAction)
        }
    }
}
