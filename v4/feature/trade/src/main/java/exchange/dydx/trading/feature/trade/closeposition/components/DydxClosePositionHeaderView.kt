package exchange.dydx.trading.feature.trade.closeposition.components

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
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketViewState

@Preview
@Composable
fun Preview_DydxClosePositionHeaderView() {
    DydxThemedPreviewSurface {
        DydxClosePositionHeaderView.Content(Modifier, DydxClosePositionHeaderView.ViewState.preview)
    }
}

object DydxClosePositionHeaderView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val sharedMarketViewState: SharedMarketViewState? = null,
        val side: SideTextView.ViewState? = null,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                sharedMarketViewState = SharedMarketViewState.preview,
                side = SideTextView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxClosePositionHeaderViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            PlatformRoundImage(
                modifier = Modifier.padding(start = ThemeShapes.HorizontalPadding),
                icon = state.sharedMarketViewState?.logoUrl,
                size = 40.dp,
            )

            Text(
                text = state.localizer.localize("APP.GENERAL.CLOSE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.extra)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )

            Spacer(modifier = Modifier.weight(1f))

            SideTextView.Content(
                Modifier,
                state.side,
                textStyle = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.medium),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = state.sharedMarketViewState?.indexPrice ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                if (state.sharedMarketViewState?.priceChangePercent24H != null) {
                    SignedAmountView.Content(
                        Modifier,
                        state.sharedMarketViewState?.priceChangePercent24H,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }
            }

            HeaderViewCloseBotton(closeAction = state.closeAction)
        }
    }
}
