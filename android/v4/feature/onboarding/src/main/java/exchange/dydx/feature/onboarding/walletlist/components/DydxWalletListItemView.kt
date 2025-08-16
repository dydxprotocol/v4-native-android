package exchange.dydx.feature.onboarding.walletlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.feature.onboarding.walletlist.components.DydxWalletListItemView.ViewState
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxWalletListItemView() {
    DydxThemedPreviewSurface {
        val viewState = ViewState(
            iconUrl = null,
            main = "main",
            trailing = "trailing",
            onTap = null,
        )
        DydxWalletListItemView(viewState).Content(Modifier)
    }
}

open class DydxWalletListItemView(
    val viewState: ViewState,
) : DydxComponent {

    data class ViewState(
        val iconUrl: Any? = null,
        val main: String,
        val trailing: String? = null,
        val onTap: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                iconUrl = null,
                main = "main",
                trailing = "trailing",
                onTap = null,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp),
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        viewState.onTap?.invoke()
                    }
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        color = ThemeColor.SemanticColor.layer_3.color,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(vertical = ThemeShapes.VerticalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))
                if (viewState.iconUrl != null) {
                    PlatformRoundImage(icon = viewState.iconUrl, size = 36.dp)
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }
                Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))
                Text(
                    style = TextStyle.dydxDefault,
                    text = viewState.main,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                )
                Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))
                Text(
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                    text = viewState.trailing ?: "",
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))
            }
        }
    }
}
