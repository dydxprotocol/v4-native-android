package exchange.dydx.trading.feature.vault.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxVaultTransferButtonView() {
    DydxThemedPreviewSurface {
        DydxVaultTransferButtonView.Content(Modifier, DydxVaultTransferButtonView.ViewState.preview)
    }
}

object DydxVaultTransferButtonView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val onTapAction: () -> Unit = {},
        val count: Int = 0,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                count = 1,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultTransferButtonViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val shape = RoundedCornerShape(10.dp)
        Row(
            modifier = modifier
                .background(
                    color = ThemeColor.SemanticColor.layer_4.color,
                    shape = shape,
                )
                .clip(shape)
                .clickable { state.onTapAction.invoke() }
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = 14.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = state.localizer.localize("APP.VAULTS.YOUR_DEPOSITS_AND_WITHDRAWALS"),
                style = TextStyle.dydxDefault,
            )

            Text(
                modifier = Modifier,
                style = TextStyle.dydxDefault
                    .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                text = state.count.toString(),
            )

            Spacer(modifier = Modifier.weight(1f))

            PlatformImage(
                icon = R.drawable.chevron_right,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_secondary.color),
            )
        }
    }
}
