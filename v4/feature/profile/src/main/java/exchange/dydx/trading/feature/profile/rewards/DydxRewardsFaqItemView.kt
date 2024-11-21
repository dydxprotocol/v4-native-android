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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformIconButton
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxRewardsFaqItemView() {
    DydxThemedPreviewSurface {
        DydxRewardsFaqItemView.Content(
            Modifier,
            DydxRewardsFaqItemView.ViewState(
                localizer = MockLocalizer(),
                question = "Question",
                answer = "Answer",
                expanded = true,
            ),
        )
    }
}

object DydxRewardsFaqItemView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val question: String,
        val answer: String,
        val expanded: Boolean = false,
        val tapped: (key: String) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                question = "Question",
                answer = "Answer",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState) {
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
    fun ListContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.localizer.localize(state.question),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_secondary),
                modifier = Modifier.weight(1f),
            )
            PlatformIconButton(
                size = 24.dp,
                action = {
                    state.tapped.invoke(state.question)
                },
                enabled = true,
                backgroundColor = ThemeColor.SemanticColor.layer_5,
            ) {
                PlatformImage(
                    modifier = Modifier.size(14.dp),
                    icon = if (state.expanded) R.drawable.icon_minus else R.drawable.icon_plus,
                )
            }
        }
        if (state.expanded) {
            Row(
                modifier = modifier,
            ) {
                Text(
                    text = state.localizer.localize(state.answer),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }
        }
    }
}
