package exchange.dydx.trading.feature.portfolio.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformPillButton
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxPortfolioHeaderView() {
    DydxThemedPreviewSurface {
        DydxPortfolioHeaderView.Content(Modifier, DydxPortfolioHeaderView.ViewState.preview)
    }
}

object DydxPortfolioHeaderView : DydxComponent {

    enum class OnboardState {
        Onboarded, NotOnboarded
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val state: OnboardState = OnboardState.Onboarded,
        val depositAction: (() -> Unit)? = null,
        val onboardAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioHeaderViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        PlatformPillButton(
            modifier = modifier,
            backgroundColor = ThemeColor.SemanticColor.layer_4,
            action = {
                when (state.state) {
                    OnboardState.Onboarded -> {
                        state.depositAction?.invoke()
                    }
                    OnboardState.NotOnboarded -> {
                        state.onboardAction?.invoke()
                    }
                }
            },
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlatformImage(
                    icon = when (state.state) {
                        OnboardState.Onboarded -> {
                            R.drawable.icon_transfer
                        }
                        OnboardState.NotOnboarded -> {
                            R.drawable.icon_wallet_connect
                        }
                    },
                    modifier = Modifier.height(14.dp),
                    colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_secondary.color),
                )

                Text(
                    text = when (state.state) {
                        OnboardState.Onboarded -> {
                            state.localizer.localize("APP.GENERAL.TRANSFER")
                        }
                        OnboardState.NotOnboarded -> {
                            state.localizer.localize("APP.ONBOARDING.GET_STARTED")
                        }
                    },
                    modifier = Modifier,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            }
        }
    }
}
