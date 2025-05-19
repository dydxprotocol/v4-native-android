package exchange.dydx.trading.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxProfileAlertsView() {
    DydxThemedPreviewSurface {
        DydxProfileAlertsView.Content(Modifier, DydxProfileAlertsView.ViewState.preview)
    }
}

object DydxProfileAlertsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val tapAction: () -> Unit = {},
        val hasAlerts: Boolean = true,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxProfileAlertsViewModel = hiltViewModel()

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
                .clickable(onClick = state.tapAction)
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(14.dp),
                ),
        ) {
            Row(
                modifier = Modifier.padding(vertical = 26.dp, horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PlatformImage(
                    modifier = Modifier
                        .size(26.dp),
                    icon = R.drawable.ic_tap_alerts,
                )
                Text(
                    text = state.localizer.localize("APP.GENERAL.ALERTS"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(
                            fontSize = ThemeFont.FontSize.base,
                        ),
                )

                Spacer(modifier = Modifier.weight(1f))

                if (state.hasAlerts) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = ThemeColor.SemanticColor.color_purple.color,
                                shape = CircleShape,
                            ),
                    )
                }
            }
        }
    }
}
