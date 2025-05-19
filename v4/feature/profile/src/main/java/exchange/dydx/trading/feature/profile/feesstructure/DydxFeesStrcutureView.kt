package exchange.dydx.trading.feature.profile.feesstructure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
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
import exchange.dydx.trading.feature.shared.views.HeaderView
import java.util.UUID

@Preview
@Composable
fun Preview_DydxFeesStrcutureView() {
    DydxThemedPreviewSurface {
        DydxFeesStrcutureView.Content(Modifier, DydxFeesStrcutureView.ViewState.preview)
    }
}

object DydxFeesStrcutureView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val tradingVolume: String? = null,
        val items: List<DydxFeesItemView.ViewState> = emptyList(),
        val backButtonAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                tradingVolume = "1.0M",
                items = listOf(
                    DydxFeesItemView.ViewState.preview,
                    DydxFeesItemView.ViewState.preview,
                    DydxFeesItemView.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxFeesStrcutureViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        Column(
            modifier = modifier
                .themeColor(ThemeColor.SemanticColor.layer_2)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.GENERAL.FEES"),
                backAction = state.backButtonAction,
            )

            LazyColumn(
                modifier = Modifier,
                state = listState,
            ) {
                item(key = "stats") {
                    CreateStatsView(
                        modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                        state = state,
                    )

                    Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                }
                item(key = "header") {
                    CreateHeader(
                        modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                        state = state,
                    )
                }

                items(items = state.items, key = { it.tier ?: UUID.randomUUID() }) { item ->
                    if (item === state.items.first()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    DydxFeesItemView.Content(
                        modifier = Modifier
                            .padding(horizontal = ThemeShapes.HorizontalPadding),
                        state = item,
                    )

                    if (item !== state.items.last()) {
                        PlatformDivider()
                    }
                }
            }
        }
    }

    @Composable
    private fun CreateStatsView(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = ThemeColor.SemanticColor.layer_4.color,
                    shape = RoundedCornerShape(9.dp),
                )
                .padding(vertical = ThemeShapes.VerticalPadding)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.TRADE.VOLUME"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )

                Text(
                    text = state.localizer.localize("APP.GENERAL.TIME_STRINGS.30D"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Text(
                text = state.tradingVolume ?: "-",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.medium)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )
        }
    }

    @Composable
    private fun CreateHeader(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.width(DydxFeesItemView.contentWidths[0]),
                text = state.localizer.localize("APP.GENERAL.TIER"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Text(
                modifier = Modifier.weight(1f),
                text = state.localizer.localize("APP.GENERAL.VOLUME_30D"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                textAlign = TextAlign.End,
            )

            Text(
                modifier = Modifier.width(DydxFeesItemView.contentWidths[2]),
                text = state.localizer.localize("APP.TRADE.MAKER_FEE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                textAlign = TextAlign.End,
            )

            Text(
                modifier = Modifier.width(DydxFeesItemView.contentWidths[3]),
                text = state.localizer.localize("APP.TRADE.TAKER_FEE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                textAlign = TextAlign.End,
            )
        }
    }
}
