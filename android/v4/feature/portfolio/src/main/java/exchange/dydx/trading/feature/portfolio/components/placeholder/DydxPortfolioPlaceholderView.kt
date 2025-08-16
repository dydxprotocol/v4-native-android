package exchange.dydx.trading.feature.portfolio.components.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxPortfolioPlaceholderView() {
    DydxThemedPreviewSurface {
        DydxPortfolioPlaceholderView.Content(
            Modifier,
            DydxPortfolioPlaceholderView.ViewState.preview,
        )
    }
}

object DydxPortfolioPlaceholderView : DydxComponent {
    enum class Selection {
        Positions, Orders, Trades, Funding, Transfer
    }

    enum class OnboardState {
        NeedWallet, NeedDeposit, Ready
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val onboardState: OnboardState = OnboardState.Ready,
        val placeholderText: String = "",
        val onboardTapAction: () -> Unit = {},
        val transferTapAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                placeholderText = "placeholderText",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioPlaceholderViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        when (state.onboardState) {
            OnboardState.NeedWallet -> {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .height(72.dp)
                        .background(
                            color = exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_3.color,
                            shape = RoundedCornerShape(12.dp),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = state.placeholderText,
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                        modifier = Modifier.padding(start = 16.dp)
                            .weight(1f),
                    )

                    PlatformButton(
                        text = state.localizer.localize("APP.TURNKEY_ONBOARD.SIGN_IN_TITLE"),
                        fontSize = ThemeFont.FontSize.base,
                        modifier = Modifier.padding(end = 16.dp)
                            .height(48.dp),
                    ) {
                        state.onboardTapAction()
                    }
                }
            }

            OnboardState.NeedDeposit -> {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .height(72.dp)
                        .background(
                            color = exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_3.color,
                            shape = RoundedCornerShape(12.dp),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = state.placeholderText,
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                        modifier = Modifier.padding(start = 16.dp)
                            .weight(1f),
                    )

                    PlatformButton(
                        text = state.localizer.localize("APP.GENERAL.DEPOSIT_FUNDS"),
                        fontSize = ThemeFont.FontSize.base,
                        modifier = Modifier.padding(end = 16.dp)
                            .height(48.dp),
                    ) {
                        state.transferTapAction()
                    }
                }
            }

            OnboardState.Ready -> {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .height(72.dp)
                        .background(
                            color = exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_3.color,
                            shape = RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.placeholderText,
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                }
            }
        }
    }
}
