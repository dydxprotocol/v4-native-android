package exchange.dydx.trading.feature.transfer.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.TokenTextView
import java.util.UUID

@Preview
@Composable
fun Preview_DydxTransferSearchItem() {
    DydxThemedPreviewSurface {
        DydxTransferSearchItem.Content(Modifier, DydxTransferSearchItem.ViewState.preview)
    }
}

object DydxTransferSearchItem {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val id: String,
        val icon: Any? = null,
        val text: String? = null,
        val tokenText: TokenTextView.ViewState? = null,
        val isSelected: Boolean = false,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                id = UUID.randomUUID().toString(),
                text = "Ethereum",
                tokenText = TokenTextView.ViewState.preview,
                icon = R.drawable.status_error,
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlatformRoundImage(
                icon = state.icon,
                size = 24.dp,
            )

            Text(
                text = state.text ?: "",
                style = TextStyle.dydxDefault,
            )

            if (state.tokenText != null) {
                TokenTextView.Content(
                    modifier = Modifier,
                    state = state.tokenText,
                    textStyle = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.mini),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (state.isSelected) {
                PlatformImage(
                    modifier = Modifier.size(20.dp),
                    icon = exchange.dydx.platformui.R.drawable.icon_check,
                    colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_primary.color),
                )
            }
        }
    }
}
