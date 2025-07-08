package exchange.dydx.trading.feature.profile.rewards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.tabgroups.PlatformPillTextGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_DydxRewardsEventsHeaderView() {
    DydxThemedPreviewSurface {
        DydxRewardsEventsHeaderView.Content(Modifier, DydxRewardsEventsHeaderView.ViewState.preview)
    }
}

object DydxRewardsEventsHeaderView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val periods: List<String>,
        val selectedIndex: Int,
        val onPeriodChanged: (Int) -> Unit,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                periods = listOf("1D", "1W", "1M", "1Y"),
                selectedIndex = 0,
                onPeriodChanged = {},
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.TRADING_REWARDS"),
                style = TextStyle.dydxDefault
                    .themeFont(
                        fontSize = ThemeFont.FontSize.small,
                        fontType = ThemeFont.FontType.book,
                    )
                    .themeColor(ThemeColor.SemanticColor.text_secondary),
            )
            PlatformPillTextGroup(
                modifier = modifier,
                items = state.periods,
                selectedItems = state.periods,
                itemStyle = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_secondary)
                    .themeFont(
                        fontType = ThemeFont.FontType.book,
                        fontSize = ThemeFont.FontSize.small,
                    ),
                selectedItemStyle = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_primary)
                    .themeFont(
                        fontType = ThemeFont.FontType.book,
                        fontSize = ThemeFont.FontSize.small,
                    ),
                currentSelection = state.selectedIndex,
                onSelectionChanged = { index ->
                    state.onPeriodChanged(index)
                },
                scrollingEnabled = true,
            )
        }

        PlatformDivider()

        DydxRewardsLayout.Content(
            modifier = modifier,
            colume1 = {
                Text(
                    text = state.localizer.localize("APP.GENERAL.TIME"),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.small,
                            fontType = ThemeFont.FontType.number,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            },
            colume2 = {
                Text(
                    text = state.localizer.localize("APP.GENERAL.TRADING_REWARDS"),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.small,
                            fontType = ThemeFont.FontType.book,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            },
        )
    }
}
