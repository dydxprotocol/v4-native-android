package exchange.dydx.trading.feature.portfolio.components.fundings

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxPortfolioFundingItemView() {
    DydxThemedPreviewSurface {
        DydxPortfolioFundingItemView.Content(
            Modifier,
            DydxPortfolioFundingItemView.ViewState.preview
        )
    }
}

object DydxPortfolioFundingItemView  {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val id: String?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                id = "1.0M",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
     //   Text(text = state?.text ?: "")
    }
}

