package exchange.dydx.trading.feature.market.marketinfo.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.dividers.PlatformVerticalDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.IntervalText
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.TokenTextView

@Preview
@Composable
fun Preview_DydxMarketStatsView() {
    DydxThemedPreviewSurface {
        DydxMarketStatsView.Content(Modifier, DydxMarketStatsView.ViewState.preview)
    }
}

object DydxMarketStatsView : DydxComponent {
    data class StatItem(
        val header: String,
        val value: SignedAmountView.ViewState? = null,
        val token: TokenTextView.ViewState? = null,
        val interval: IntervalText.ViewState? = null,
    ) {
        companion object {
            val preview = StatItem(
                header = "24h Volume",
                value = SignedAmountView.ViewState.preview,
                token = TokenTextView.ViewState.preview,
            )
        }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val statItems: List<StatItem> = emptyList(),
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                statItems = listOf(
                    StatItem.preview,
                    StatItem.preview,
                    StatItem.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketStatsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        var rows: MutableList<List<StatItem>> = mutableListOf()
        for (i in 0..state.statItems.size step 2) {
            val end = i + 2
            if (end > state.statItems.size) {
                rows.add(state.statItems.subList(i, state.statItems.size))
            } else {
                rows.add(state.statItems.subList(i, end))
            }
        }

        Column(modifier = modifier) {
            PlatformDivider()
            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    row.forEachIndexed { index, statItem ->
                        if (index == 1) {
                            PlatformVerticalDivider()
                        }
                        Column(
                            modifier = Modifier
                                .padding(horizontal = ThemeShapes.HorizontalPadding)
                                .padding(vertical = ThemeShapes.VerticalPadding)
                                .weight(1f),
                        ) {
                            Text(
                                text = statItem.header,
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.small)
                                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                            )
                            Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                            Row(
                                modifier = Modifier,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (statItem.interval != null) {
                                    IntervalText.Content(
                                        Modifier,
                                        statItem.interval,
                                        TextStyle.dydxDefault.themeFont(
                                            fontSize = ThemeFont.FontSize.medium,
                                            fontType = ThemeFont.FontType.plus,
                                        ),
                                    )
                                } else {
                                    SignedAmountView.Content(
                                        Modifier,
                                        statItem.value,
                                        TextStyle.dydxDefault.themeFont(
                                            fontSize = ThemeFont.FontSize.medium,
                                            fontType = ThemeFont.FontType.plus,
                                        ),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    TokenTextView.Content(
                                        Modifier,
                                        statItem.token,
                                        textStyle = TextStyle.dydxDefault
                                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                                            .themeColor(ThemeColor.SemanticColor.text_primary),
                                    )
                                }
                            }
                        }
                    }
                }

                if (row != rows.last()) {
                    PlatformDivider()
                }
            }
        }
    }
}
