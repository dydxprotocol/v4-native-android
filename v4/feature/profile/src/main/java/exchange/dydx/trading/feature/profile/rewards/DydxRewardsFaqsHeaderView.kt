package exchange.dydx.trading.feature.profile.rewards

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer

@Preview
@Composable
fun Preview_DydxRewardsFaqsHeaderView() {
    DydxThemedPreviewSurface {
        DydxRewardsFaqsHeaderView.Content(Modifier, DydxRewardsFaqsHeaderView.ViewState.preview)
    }
}

object DydxRewardsFaqsHeaderView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val title: String,
        val learnMoreText: String?,
        val link: String?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                title = "FAQs",
                learnMoreText = "Learn more",
                link = "https://dydx.exchange",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState) {
        DydxRewardsLayout.Content(
            modifier = modifier,
            colume1 = {
                Text(
                    text = state.title,
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.small,
                            fontType = ThemeFont.FontType.book,
                        )
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
            },
            colume2 = {
                state.learnMoreText?.let {
                    Text(
                        text = it,
                        style = TextStyle.dydxDefault
                            .themeFont(
                                fontSize = ThemeFont.FontSize.small,
                                fontType = ThemeFont.FontType.book,
                            )
                            .themeColor(ThemeColor.SemanticColor.text_secondary),
                    )
                }
            },
        )
    }
}
