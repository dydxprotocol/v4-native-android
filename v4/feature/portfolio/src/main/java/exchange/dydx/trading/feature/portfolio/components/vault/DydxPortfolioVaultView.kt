package exchange.dydx.trading.feature.portfolio.components.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.gradient.GradientType
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.SignedAmountView

@Preview
@Composable
fun Preview_DydxPortfolioVaultView() {
    DydxThemedPreviewSurface {
        DydxPortfolioVaultView.Content(Modifier, DydxPortfolioVaultView.ViewState.preview)
    }
}

object DydxPortfolioVaultView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val gradientType: GradientType = GradientType.NONE,
        val onTapAction: () -> Unit = {},
        val balance: String? = null,
        val apr: SignedAmountView.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                balance = "$1,234.56",
                apr = SignedAmountView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioVaultViewModel = hiltViewModel()

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
                .padding(horizontal = ThemeShapes.HorizontalPadding, vertical = ThemeShapes.VerticalPadding),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            Text(
                text = state.localizer.localize("APP.VAULTS.MEGAVAULT"),
                modifier = Modifier
                    .padding(vertical = ThemeShapes.VerticalPadding),
                style = TextStyle.dydxDefault
                    .themeColor(foreground = ThemeColor.SemanticColor.text_primary)
                    .themeFont(fontSize = ThemeFont.FontSize.large, fontType = ThemeFont.FontType.plus),
            )

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.DETAILS"),
                    modifier = Modifier
                        .weight(1.25f),
                    style = TextStyle.dydxDefault
                        .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )

                Text(
                    text = state.localizer.localize("APP.VAULTS.VAULT_THIRTY_DAY_APR"),
                    modifier = Modifier
                        .weight(1f),
                    style = TextStyle.dydxDefault
                        .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )

                Text(
                    text = state.localizer.localize("APP.VAULTS.YOUR_VAULT_BALANCE"),
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f),
                    style = TextStyle.dydxDefault
                        .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            }

            val shape = RoundedCornerShape(10.dp)

            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(64.dp)
                    .background(
                        brush = state.gradientType.brush(ThemeColor.SemanticColor.layer_3),
                        shape = shape,
                    )
                    .clip(shape)
                    .clickable { state.onTapAction.invoke() }
                    .padding(
                        // inner paddings after clipping
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1.25f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlatformImage(
                        modifier = Modifier
                            .size(32.dp),
                        icon = R.drawable.vault_account_token,
                    )

                    Text(
                        text = state.localizer.localize("APP.VAULTS.MEGAVAULT"),
                        modifier = Modifier,
                        style = TextStyle.dydxDefault
                            .themeColor(foreground = ThemeColor.SemanticColor.text_secondary)
                            .themeFont(fontSize = ThemeFont.FontSize.base),
                    )
                }

                SignedAmountView.Content(
                    modifier = Modifier.weight(1f),
                    state = state.apr,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini),
                )

                Text(
                    text = state.balance ?: "-",
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                    style = TextStyle.dydxDefault
                        .themeColor(foreground = ThemeColor.SemanticColor.text_secondary)
                        .themeFont(fontSize = ThemeFont.FontSize.base),
                )
            }
        }
    }
}
