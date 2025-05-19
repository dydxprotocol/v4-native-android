package exchange.dydx.trading.feature.trade.tradeinput.components.sheettip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.AmountText
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SizeTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView

@Preview
@Composable
fun Preview_DydxTradeSheetTipDraftView() {
    DydxThemedPreviewSurface {
        DydxTradeSheetTipDraftView.Content(Modifier, DydxTradeSheetTipDraftView.ViewState.preview)
    }
}

object DydxTradeSheetTipDraftView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val type: String? = null,
        val side: SideTextView.ViewState? = null,
        val size: SizeTextView.ViewState? = null,
        val token: TokenTextView.ViewState? = null,
        val price: AmountText.ViewState? = null,
        val onTapAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                type = "Limit",
                side = SideTextView.ViewState.preview,
                size = SizeTextView.ViewState.preview,
                token = TokenTextView.ViewState.preview,
                price = AmountText.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeSheetTipDraftViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier
                .clickable(onClick = state.onTapAction ?: {})
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.RETURN_TO_DRAFT"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )

                Spacer(modifier = Modifier.weight(1f))

                SizeTextView.Content(
                    Modifier,
                    state.size,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                TokenTextView.Content(
                    Modifier,
                    state.token,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.type ?: "",
                    style = TextStyle.dydxDefault,
                )

                SideTextView.Content(
                    Modifier,
                    state.side,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny),
                )

                Spacer(modifier = Modifier.weight(1f))

                AmountText.Content(
                    Modifier,
                    state.price,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }
        }
    }
}
