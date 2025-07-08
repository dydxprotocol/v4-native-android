package exchange.dydx.trading.feature.trade.tradestatus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.DydxReceiptView
import exchange.dydx.trading.feature.trade.tradestatus.components.DydxTradeStatusCtaButtonView
import exchange.dydx.trading.feature.trade.tradestatus.components.DydxTradeStatusHeaderView
import exchange.dydx.trading.feature.trade.tradestatus.components.DydxTradeStatusPositionView

@Preview
@Composable
fun Preview_DydxTradeStatusView() {
    DydxThemedPreviewSurface {
        DydxTradeStatusView.Content(Modifier, DydxTradeStatusView.ViewState.preview)
    }
}

object DydxTradeStatusView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeStatusViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            DydxTradeStatusHeaderView.Content(modifier = Modifier)

            PlatformDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                DydxTradeStatusPositionView.Content(modifier = Modifier)
            }

            Column {
                DydxReceiptView.Content(Modifier.offset(y = ThemeShapes.VerticalPadding))
                DydxTradeStatusCtaButtonView.Content(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(bottom = ThemeShapes.VerticalPadding * 2),
                )
            }
        }
    }
}
