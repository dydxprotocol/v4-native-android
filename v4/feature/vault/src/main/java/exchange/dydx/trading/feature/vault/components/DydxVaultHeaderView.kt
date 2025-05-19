package exchange.dydx.trading.feature.vault.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformIconButton
import exchange.dydx.platformui.components.icons.PlatformImage
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
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxVaultHeaderView() {
    DydxThemedPreviewSurface {
        DydxVaultHeaderView.Content(Modifier, DydxVaultHeaderView.ViewState.preview)
    }
}

object DydxVaultHeaderView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val learnMoreAction: () -> Unit = {},
        val dydxChainLogoUrl: String? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                dydxChainLogoUrl = "https://media.dydx.exchange/currencies/eth.png",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultHeaderViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(vertical = ThemeShapes.VerticalPadding)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            PlatformImage(
                icon = state.dydxChainLogoUrl,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
            )

            Text(
                text = state.localizer.localize("APP.VAULTS.MEGAVAULT"),
                modifier = Modifier,
                style = TextStyle.dydxDefault
                    .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.extra)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                PlatformIconButton(
                    action = state.learnMoreAction,
                    size = 42.dp,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_info),
                        contentDescription = state.localizer.localize("APP.GENERAL.LEARN_MORE"),
                        modifier = Modifier.size(24.dp),
                        tint = ThemeColor.SemanticColor.text_secondary.color,
                    )
                }
            }
        }
    }
}
