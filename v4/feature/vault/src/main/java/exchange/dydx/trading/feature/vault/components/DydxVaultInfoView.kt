package exchange.dydx.trading.feature.vault.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
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
import exchange.dydx.trading.feature.shared.views.SignedAmountView

@Preview
@Composable
fun Preview_DydxVaultInfoView() {
    DydxThemedPreviewSurface {
        DydxVaultInfoView.Content(Modifier, DydxVaultInfoView.ViewState.preview)
    }
}

object DydxVaultInfoView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val balance: String? = null,
        val pnl: SignedAmountView.ViewState? = null,
        val apr: SignedAmountView.ViewState? = null,
        val tvl: String? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                balance = "$1.0M",
                pnl = SignedAmountView.ViewState.preview,
                apr = SignedAmountView.ViewState.preview,
                tvl = "$1.0M",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultInfoViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier,
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TopRowItem(
                    modifier = Modifier.weight(1f),
                    title = state.localizer.localize("APP.VAULTS.YOUR_VAULT_BALANCE"),
                    valueComposable = {
                        Text(
                            text = state.balance ?: "-",
                            modifier = Modifier,
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.medium)
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    },
                )
                TopRowItem(
                    modifier = Modifier.weight(1f),
                    title = state.localizer.localize("APP.VAULTS.YOUR_ALL_TIME_PNL"),
                    valueComposable = {
                        SignedAmountView.Content(
                            modifier = Modifier,
                            state = state.pnl,
                            textStyle = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.medium),
                        )
                    },
                )
            }

            PlatformDivider()

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BottomRowItem(
                    modifier = Modifier,
                    title = state.localizer.localize("APP.VAULTS.VAULT_THIRTY_DAY_APR"),
                    valueComposable = {
                        if (state.apr != null) {
                            SignedAmountView.Content(
                                modifier = Modifier,
                                state = state.apr,
                                textStyle = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.medium),
                            )
                        } else {
                            Text(
                                text = "-",
                                modifier = Modifier,
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.medium)
                                    .themeColor(ThemeColor.SemanticColor.text_primary),
                            )
                        }
                    },
                )
                BottomRowItem(
                    modifier = Modifier.weight(1f),
                    title = state.localizer.localize("APP.VAULTS.TVL"),
                    valueComposable = {
                        Text(
                            text = state.tvl ?: "-",
                            modifier = Modifier,
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.medium)
                                .themeColor(ThemeColor.SemanticColor.text_primary),
                        )
                    },
                )
            }

            PlatformDivider()
        }
    }

    @Composable
    private fun TopRowItem(modifier: Modifier, title: String, valueComposable: @Composable (Modifier) -> Unit) {
        val shape = RoundedCornerShape(size = 10.dp)
        Column(
            modifier = modifier
                .border(
                    width = 1.dp,
                    color = ThemeColor.SemanticColor.border_default.color,
                    shape = shape,
                )
                .clip(shape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            Text(
                text = title,
                modifier = Modifier,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.base)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            valueComposable(Modifier)
        }
    }

    @Composable
    private fun BottomRowItem(modifier: Modifier, title: String, valueComposable: @Composable (Modifier) -> Unit) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            Text(
                text = title,
                modifier = Modifier,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.base)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
            valueComposable(Modifier)
        }
    }
}
