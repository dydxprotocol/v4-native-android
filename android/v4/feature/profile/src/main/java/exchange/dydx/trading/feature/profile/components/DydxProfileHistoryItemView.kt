package exchange.dydx.trading.feature.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.profile.components.DydxProfileHistoryItemView.ViewState.Companion.itemRatios
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView

@Preview
@Composable
fun Preview_DydxProfileHistoryItemView() {
    DydxThemedPreviewSurface {
        DydxProfileHistoryItemView.Content(Modifier, DydxProfileHistoryItemView.ViewState.preview)
    }
}

object DydxProfileHistoryItemView {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val action: ActionType?,
        val side: SideTextView.ViewState?,
        val type: TypeUnion?,
        val amount: String?
    ) {
        sealed class ActionType {
            data class Fill(val sideTextViewModel: SideTextView.ViewState?, val string: kotlin.String) : ActionType()
            data class String(val string: kotlin.String) : ActionType()
        }

        sealed class TypeUnion {
            data class Token(val tokenTextViewModel: TokenTextView.ViewState) : TypeUnion()
            data class String(val string: kotlin.String) : TypeUnion()
        }

        companion object {
            val itemRatios = listOf(0.3f, 0.2f, 0.2f, 0.3f)

            val preview = ViewState(
                localizer = MockLocalizer(),
                action = ActionType.Fill(
                    sideTextViewModel = SideTextView.ViewState(
                        side = SideTextView.Side.Buy,
                        localizer = MockLocalizer(),
                    ),
                    string = "ETH-USD",
                ),
                side = SideTextView.ViewState(
                    side = SideTextView.Side.Buy,
                    localizer = MockLocalizer(),
                ),
                type = TypeUnion.Token(
                    tokenTextViewModel = TokenTextView.ViewState(
                        symbol = "ETH",
                    ),
                ),
                amount = "0.000",
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
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            when (state.action) {
                is ViewState.ActionType.Fill -> {
                    Row(
                        modifier = Modifier.weight(itemRatios[0]),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SideTextView.Content(
                            modifier = Modifier,
                            state = state.action.sideTextViewModel,
                            textStyle = TextStyle.dydxDefault.themeFont(fontSize = ThemeFont.FontSize.mini),
                        )
                        TokenTextView.Content(
                            modifier = Modifier,
                            state = TokenTextView.ViewState(
                                symbol = state.action.string,
                            ),
                            textStyle = TextStyle.dydxDefault.themeFont(fontSize = ThemeFont.FontSize.tiny),
                        )
                    }
                }

                is ViewState.ActionType.String -> {
                    Text(
                        modifier = Modifier.weight(itemRatios[0]),
                        text = state.action.string,
                        style = TextStyle.dydxDefault.themeFont(fontSize = ThemeFont.FontSize.mini),
                    )
                }

                else -> {
                    Text(
                        modifier = Modifier.weight(itemRatios[0]),
                        text = "-",
                        style = TextStyle.dydxDefault.themeFont(fontSize = ThemeFont.FontSize.mini),
                    )
                }
            }

            Row(
                modifier = Modifier.weight(itemRatios[1]),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                SideTextView.Content(
                    modifier = Modifier,
                    state = state.side,
                    textStyle = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.mini),
                )
            }

            when (state.type) {
                is ViewState.TypeUnion.Token -> {
                    Row(
                        modifier = Modifier.weight(itemRatios[2]),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TokenTextView.Content(
                            modifier = Modifier,
                            state = state.type.tokenTextViewModel,
                            textStyle = TextStyle.dydxDefault.themeFont(fontSize = ThemeFont.FontSize.tiny),
                        )
                    }
                }

                is ViewState.TypeUnion.String -> {
                    Text(
                        modifier = Modifier.weight(itemRatios[2]),
                        text = state.type.string,
                        style = TextStyle.dydxDefault.themeFont(fontSize = ThemeFont.FontSize.mini),
                        textAlign = TextAlign.Center,
                    )
                }

                else -> {
                    Text(
                        modifier = Modifier.weight(itemRatios[2]),
                        text = "-",
                        style = TextStyle.dydxDefault.themeFont(fontSize = ThemeFont.FontSize.mini),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Text(
                modifier = Modifier.weight(itemRatios[3]),
                text = state.amount ?: "-",
                style = TextStyle.dydxDefault.themeFont(fontSize = ThemeFont.FontSize.mini),
                textAlign = TextAlign.End,
            )
        }
    }
}
