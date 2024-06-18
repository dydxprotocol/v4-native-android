package exchange.dydx.trading.feature.profile.rewards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont

object DydxRewardsEventItemView {
    enum class DydxRewardsPeriod {
        DAILY,
        WEEKLY,
        MONTHLY,
    }

    data class ViewState(
        val timeText: String,
        val amountText: String,
        val nativeTokenLogoUrl: String? = null,
    ) {
        companion object {
            val preview = ViewState(
                timeText = "Jan 1th, 2021",
                amountText = "0.0032",
                nativeTokenLogoUrl = "https://v4-testnet.vercel.app/currencies/dydx.png",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: DydxRewardsEventItemView.ViewState) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ListContent(modifier = Modifier.fillMaxWidth(), state = state)
        }
    }

    @Composable
    fun ListContent(modifier: Modifier, state: DydxRewardsEventItemView.ViewState) {
        Row(
            modifier = Modifier.padding(
                horizontal = 0.dp,
                vertical = 0.dp,
            ),
        ) {
            DydxRewardsLayout.Content(
                modifier = modifier,
                colume1 = {
                    Text(
                        text = state.timeText,
                        style = TextStyle.dydxDefault
                            .themeFont(
                                fontSize = ThemeFont.FontSize.small,
                                fontType = ThemeFont.FontType.book,
                            )
                            .themeColor(ThemeColor.SemanticColor.text_secondary),
                    )
                },
                colume2 = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = state.amountText,
                            style = TextStyle.dydxDefault
                                .themeFont(
                                    fontSize = ThemeFont.FontSize.small,
                                    fontType = ThemeFont.FontType.book,
                                )
                                .themeColor(ThemeColor.SemanticColor.text_secondary),
                        )
                        PlatformImage(
                            icon = state.nativeTokenLogoUrl,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        }
    }
}
