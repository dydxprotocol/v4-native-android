package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_LeverageRiskView() {
    DydxThemedPreviewSurface {
        LeverageRiskView.Content(Modifier, LeverageRiskView.ViewState.preview)
    }
}

object LeverageRiskView {
    enum class Level {
        LOW, MEDIUM, HIGH;

        val stringKey: String
            get() = when (this) {
                LOW -> "APP.TRADE.LOW"
                MEDIUM -> "APP.TRADE.MEDIUM"
                HIGH -> "APP.TRADE.HIGH"
            }

        val image: Any
            get() = when (this) {
                LOW -> R.drawable.leverage_low
                MEDIUM -> R.drawable.leverage_medium
                HIGH -> R.drawable.leverage_high
            }

        companion object {
            fun createFromMarginUsage(marginUsage: Double): Level {
                return when {
                    marginUsage <= 0.2 -> LOW
                    marginUsage <= 0.4 -> MEDIUM
                    else -> HIGH
                }
            }
        }
    }

    enum class DisplayOption {
        IconOnly, IconAndText
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val level: Level = Level.LOW,
        val viewSize: Dp = 32.dp,
        val displayOption: DisplayOption = DisplayOption.IconAndText,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlatformImage(
                icon = state.level.image,
                modifier = Modifier.size(state.viewSize),
            )

            when (state.displayOption) {
                DisplayOption.IconOnly -> {
                    // No-op
                }

                DisplayOption.IconAndText -> {
                    Text(
                        text = state.localizer.localize(state.level.stringKey),
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }
            }
        }
    }
}
