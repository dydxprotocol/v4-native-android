package exchange.dydx.trading.feature.newsalerts.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.compose.PlatformRememberLazyListState
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
import exchange.dydx.trading.feature.newsalerts.alerts.alertprovider.DydxAlertsProviderItemProtocol
import java.util.Date

@Preview
@Composable
fun Preview_DydxAlertsView() {
    DydxThemedPreviewSurface {
        DydxAlertsView.Content(Modifier, DydxAlertsView.ViewState.preview)
    }
}

object DydxAlertsView : DydxComponent {
    data class Item(
        override val title: String? = null,
        override val message: String? = null,
        override val icon: Any? = null,
        override val tapAction: (() -> Unit)? = null,
        override val date: Date? = null,
    ) : DydxAlertsProviderItemProtocol {
        companion object {
            val preview = Item(
                title = "title",
                message = "message",
                icon = null,
                tapAction = null,
            )
        }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val items: List<Item> = emptyList(),
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                items = listOf(
                    Item.preview,
                    Item.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAlertsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val listState = PlatformRememberLazyListState(key = "DydxAlertsView")

        if (state.items.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(ThemeShapes.HorizontalPadding),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = state.localizer.localize("APP.V4.ALERTS_PLACHOLDER"),
                    style = TextStyle.dydxDefault,
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .padding(ThemeShapes.HorizontalPadding),
                state = listState,
            ) {
                items(state.items.count()) { index ->
                    val item = state.items[index]
                    ItemView(
                        modifier = Modifier,
                        item = item,
                    )
                    Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                }
            }
        }
    }

    @Composable
    private fun ItemView(
        modifier: Modifier,
        item: Item,
    ) {
        val shape = RoundedCornerShape(12.dp)

        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(color = ThemeColor.SemanticColor.layer_3.color, shape = shape)
                .border(2.dp, ThemeColor.SemanticColor.layer_6.color, shape = shape)
                .clip(shape)
                .clickable {
                    item.tapAction?.invoke()
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlatformImage(
                modifier = Modifier
                    .size(24.dp),
                icon = item.icon,
            )

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (item.title != null) {
                    Text(
                        text = item.title,
                        style = TextStyle.dydxDefault,
                    )
                }

                if (item.message != null) {
                    Text(
                        text = item.message,
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }
            }
        }
    }
}
