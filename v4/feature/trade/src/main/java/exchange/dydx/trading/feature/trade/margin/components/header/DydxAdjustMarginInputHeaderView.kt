package exchange.dydx.trading.feature.trade.margin.components.header

import androidx.compose.foundation.layout.Arrangement
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

@Preview
@Composable
fun Preview_DydxAdjustMarginInputHeaderView() {
    DydxThemedPreviewSurface {
        DydxAdjustMarginInputHeaderView.Content(
            Modifier,
            DydxAdjustMarginInputHeaderView.ViewState.preview,
        )
    }
}

object DydxAdjustMarginInputHeaderView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val logoUrl: String? = null,
        val closeAction: (() -> Unit) = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxAdjustMarginInputHeaderViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            PlatformRoundImage(
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                icon = state.logoUrl,
                size = 40.dp,
            )

            Text(
                modifier = Modifier.padding(horizontal = 0.dp),
                style = TextStyle.dydxDefault
                    .themeFont(
                        fontSize = ThemeFont.FontSize.large,
                        fontType = ThemeFont.FontType.plus,
                    )
                    .themeColor(ThemeColor.SemanticColor.text_primary),
                text = state.localizer.localize("APP.TRADE.ADJUST_ISOLATED_MARGIN"),
            )
            Spacer(modifier = Modifier.weight(1f))
            HeaderViewCloseBotton(
                closeAction = state.closeAction,
            )
        }
    }
}
