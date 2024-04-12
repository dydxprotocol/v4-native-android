package exchange.dydx.trading.feature.trade.tradeinput

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.platformui.components.PlatformInfoScaffold
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton

data class LeverageTextAndValue(val text: String, val value: Double)

@Preview
@Composable
fun Preview_DydxTradeInputTargetLeverageView() {
    DydxThemedPreviewSurface {
        DydxTradeInputTargetLeverageView.Content(
            Modifier,
            DydxTradeInputTargetLeverageView.ViewState.preview,
        )
    }
}

object DydxTradeInputTargetLeverageView : DydxComponent {
    data class ViewState(
        val title: String?,
        val text: String?,
        val leverageText: String?,
        val leverageOptions: List<LeverageTextAndValue>?,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                title = "title",
                text = "text",
                leverageText = "1.0",
                leverageOptions = listOf(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeInputTargetLeverageViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        PlatformInfoScaffold(modifier = modifier, platformInfo = viewModel.platformInfo) {
            Content(it, state)
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .animateContentSize()
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_3),
        ) {
            Row(
                modifier
                    .fillMaxWidth()
                    .padding(vertical = ThemeShapes.VerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                HeaderViewCloseBotton(
                    closeAction = state.closeAction,
                )
            }

            PlatformDivider()
        }
    }
}
