package exchange.dydx.trading.feature.vault.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
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
import exchange.dydx.trading.feature.vault.components.DydxVaultPositionItemView.chartWidth

@Preview
@Composable
fun Preview_DyxVaultPositionsHeaderView() {
    DydxThemedPreviewSurface {
        DydxVaultPositionsHeaderView.Content(Modifier, DydxVaultPositionsHeaderView.ViewState.preview)
    }
}

object DydxVaultPositionsHeaderView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val positionCount: Int = 0,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                positionCount = 2,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultPositionsHeaderViewModel = hiltViewModel()

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
                .fillMaxWidth(),
        ) {
            PositionsContent(modifier, state)

            ItemTableHeaderContent(modifier, state)
        }
    }

    @Composable
    private fun PositionsContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .padding(top = ThemeShapes.VerticalPadding * 2, bottom = ThemeShapes.VerticalPadding)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                style = TextStyle.dydxDefault
                    .themeFont(
                        fontSize = ThemeFont.FontSize.large,
                    ),
                text = state.localizer.localize("APP.VAULTS.HOLDINGS"),
            )

            val shape = RoundedCornerShape(size = 4.dp)
            Text(
                modifier = Modifier
                    .clip(shape)
                    .background(ThemeColor.SemanticColor.layer_6.color)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                style = TextStyle.dydxDefault
                    .themeFont(
                        fontSize = ThemeFont.FontSize.small,
                    ),
                text = state.positionCount.toString(),
            )
        }
    }

    @Composable
    private fun ItemTableHeaderContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding, vertical = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.width(DydxVaultPositionItemView.marketSectionWidth),
                text = state.localizer.localize("APP.GENERAL.MARKET"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Row() {
                Text(
                    text = state.localizer.localize("APP.GENERAL.SIZE"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )

                Text(
                    text = state.localizer.localize(" / "),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )

                Text(
                    text = state.localizer.localize("APP.GENERAL.EQUITY"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = state.localizer.localize("APP.VAULTS.VAULT_THIRTY_DAY_PNL"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Spacer(modifier = Modifier.width(chartWidth))
        }
    }
}
