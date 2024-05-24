package exchange.dydx.trading.feature.profile.feesstructure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer

@Preview
@Composable
fun Preview_DydxFeesItemView() {
    DydxThemedPreviewSurface {
        DydxFeesItemView.Content(Modifier, DydxFeesItemView.ViewState.preview)
    }
}

object DydxFeesItemView {

    val contentWidths = listOf(80.dp, 0.dp, 70.dp, 70.dp)

    data class Condition(
        val title: String?,
        val value: String,
    )

    data class ViewState(
        val localizer: LocalizerProtocol,
        val tier: String?,
        val conditions: List<Condition> = emptyList(),
        val makerPercent: String? = null,
        val takerPercent: String? = null,
        val isUserTier: Boolean = false,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                tier = "Tier 1",
                conditions = listOf(
                    Condition(
                        title = "24h Volume",
                        value = "> 0.0",
                    ),
                    Condition(
                        title = "Maker Fee",
                        value = "> 0.0%",
                    ),
                    Condition(
                        title = "Taker Fee",
                        value = "> 0.0%",
                    ),
                ),
                makerPercent = "0.0%",
                takerPercent = "0.0%",
                isUserTier = true,
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.width(contentWidths[0]),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.tier ?: "-",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini),
                )

                if (state.isUserTier) {
                    Text(
                        modifier = Modifier
                            .background(
                                ThemeColor.SemanticColor.layer_6.color,
                                RoundedCornerShape(50),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        text = state.localizer.localize("APP.GENERAL.YOU"),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.tiny),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                state.conditions.forEach { condition ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        val value = buildAnnotatedString {
                            if (condition.title != null) {
                                pushStyle(SpanStyle(color = ThemeColor.SemanticColor.text_tertiary.color))
                                append(condition.title)
                                pop()
                            }

                            pushStyle(SpanStyle(color = ThemeColor.SemanticColor.text_primary.color))
                            append(" " + condition.value)
                            pop()
                            toAnnotatedString()
                        }

                        Text(
                            text = value,
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.mini),
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }

            Text(
                modifier = Modifier.width(contentWidths[2]),
                text = state.makerPercent ?: "-",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini),
                textAlign = TextAlign.End,
            )

            Text(
                modifier = Modifier.width(contentWidths[3]),
                text = state.takerPercent ?: "-",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini),
                textAlign = TextAlign.End,
            )
        }
    }
}
