package exchange.dydx.trading.feature.market.marketinfo.components.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.dividers.PlatformVerticalDivider
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.market.marketinfo.components.diff.DydxDiffView

@Preview
@Composable
fun Preview_DydxMarketAccountView() {
    DydxThemedPreviewSurface {
        DydxMarketAccountView.Content(Modifier.height(240.dp), DydxMarketAccountView.ViewState.preview)
    }
}

object DydxMarketAccountView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,

        val buyingPower: DydxDiffView.ViewState?,
        val marginUsage: DydxDiffView.ViewState?,
        val equity: DydxDiffView.ViewState?,
        val freeCollateral: DydxDiffView.ViewState?,
        val openInterest: DydxDiffView.ViewState?,
        val accountLeverage: DydxDiffView.ViewState?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                buyingPower = DydxDiffView.ViewState.preview,
                marginUsage = DydxDiffView.ViewState.preview,
                equity = DydxDiffView.ViewState.preview,
                freeCollateral = DydxDiffView.ViewState.preview,
                openInterest = DydxDiffView.ViewState.preview,
                accountLeverage = DydxDiffView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketAccountViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight(1f / 3f),
            ) {
                DydxDiffView.Content(
                    modifier = Modifier.fillMaxWidth(0.5f).padding(8.dp),
                    state = state?.buyingPower,
                )

                PlatformVerticalDivider()

                DydxDiffView.Content(
                    modifier = Modifier.padding(8.dp),
                    state = state?.marginUsage,
                )
            }

            PlatformDivider()

            Row(
                modifier = Modifier
                    .fillMaxHeight(0.5f),
            ) {
                DydxDiffView.Content(
                    modifier = Modifier.fillMaxWidth(0.5f).padding(8.dp),
                    state = state?.equity,
                )

                PlatformVerticalDivider()

                DydxDiffView.Content(
                    modifier = Modifier.padding(8.dp),
                    state = state?.freeCollateral,
                )
            }

            PlatformDivider()

            Row(
                modifier = Modifier
                    .fillMaxHeight(),
            ) {
                DydxDiffView.Content(
                    modifier = Modifier.fillMaxWidth(0.5f).padding(8.dp),
                    state = state?.openInterest,
                )

                PlatformVerticalDivider()

                DydxDiffView.Content(
                    modifier = Modifier.padding(8.dp),
                    state = state?.accountLeverage,
                )
            }
        }
    }
}
