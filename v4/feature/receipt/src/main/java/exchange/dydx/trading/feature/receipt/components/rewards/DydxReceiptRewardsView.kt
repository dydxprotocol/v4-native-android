package exchange.dydx.trading.feature.receipt.components.rewards

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.feature.receipt.components.DydxReceiptEmptyView
import exchange.dydx.trading.feature.shared.views.SignedAmountView

@Preview
@Composable
fun Preview_DydxReceiptRewardsView() {
    DydxThemedPreviewSurface {
        DydxReceiptRewardsView.Content(Modifier, DydxReceiptRewardsView.ViewState.preview)
    }
}

object DydxReceiptRewardsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val nativeTokenLogoUrl: String? = null,
        val rewards: SignedAmountView.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                rewards = SignedAmountView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxReceiptRewardsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.MAXIMUM_REWARDS"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            if (state.nativeTokenLogoUrl != null) {
                Spacer(modifier = Modifier.width(4.dp))
                PlatformRoundImage(
                    icon = state.nativeTokenLogoUrl,
                    size = 18.dp,
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            if (state.rewards != null) {
                SignedAmountView.Content(
                    modifier = Modifier,
                    state = state.rewards,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small, fontType = ThemeFont.FontType.number)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
            } else {
                DydxReceiptEmptyView()
            }
        }
    }
}
