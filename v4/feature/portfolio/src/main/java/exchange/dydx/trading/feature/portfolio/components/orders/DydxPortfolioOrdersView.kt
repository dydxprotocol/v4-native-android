package exchange.dydx.trading.feature.portfolio.components.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
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
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.portfolio.components.DydxPortfolioSelectorView
import exchange.dydx.trading.feature.portfolio.components.orders.DydxPortfolioOrdersView.ordersListContent
import exchange.dydx.trading.feature.portfolio.components.placeholder.DydxPortfolioPlaceholderView
import exchange.dydx.trading.feature.shared.views.HeaderView
import exchange.dydx.trading.feature.shared.viewstate.SharedOrderViewState

@Preview
@Composable
fun Preview_DydxPortfolioOrdersView() {
    DydxThemedPreviewSurface {
        LazyColumn {
            this.ordersListContent(
                DydxPortfolioOrdersView.ViewState.preview,
            )
        }
    }
}

object DydxPortfolioOrdersView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val orders: List<SharedOrderViewState> = listOf(),
        val onOrderTappedAction: (String) -> Unit = {},
        val onBackTappedAction: () -> Unit = {},
        val onboarded: Boolean,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                orders = listOf(
                    SharedOrderViewState.preview,
                    SharedOrderViewState.preview,
                ),
                onboarded = true,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        Content(modifier, isFullScreen = false, showPortfolioSelector = false)
    }

    @Composable
    fun Content(modifier: Modifier, isFullScreen: Boolean, showPortfolioSelector: Boolean) {
        val viewModel: DydxPortfolioOrdersViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        if (isFullScreen) {
            Column(
                modifier = modifier.fillMaxWidth()
                    .themeColor(ThemeColor.SemanticColor.layer_2),
            ) {
                if (showPortfolioSelector) {
                    DydxPortfolioSelectorView.Content(
                        modifier = Modifier
                            .height(72.dp)
                            .padding(horizontal = ThemeShapes.HorizontalPadding)
                            .fillMaxWidth(),
                    )
                } else {
                    HeaderView(
                        modifier = Modifier.fillMaxWidth(),
                        title = state?.localizer?.localize("APP.GENERAL.ORDERS") ?: "",
                        backAction = state?.onBackTappedAction,
                    )
                }

                PlatformDivider()

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    ordersListContent(state)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier,
            ) {
                ordersListContent(state)
            }
        }
    }

    fun LazyListScope.ordersListContent(state: ViewState?) {
        if (state == null) return

        if (state.orders.isEmpty()) {
            item(key = "placeholder") {
                DydxPortfolioPlaceholderView.Content(Modifier.padding(vertical = 0.dp))
            }
        } else {
            item(key = "header") {
                CreateHeader(Modifier, state)
            }

            items(items = state.orders, key = { it.id }) { order ->
                if (order === state.orders.first()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                DydxPortfolioOrderItemView.Content(
                    modifier = Modifier
                        .clickable { state.onOrderTappedAction(order.id) },
                    state = order,
                )

                if (order !== state.orders.last()) {
                    PlatformDivider()
                }
            }
        }
    }

    @Composable
    private fun CreateHeader(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding * 2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.STATUS_FILL"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = state.localizer.localize("APP.GENERAL.PRICE_TYPE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }
}
