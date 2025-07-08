package exchange.dydx.trading.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxProfileBalancesView() {
    DydxThemedPreviewSurface {
        DydxProfileBalancesView.Content(Modifier, DydxProfileBalancesView.ViewState.preview)
    }
}

object DydxProfileBalancesView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val walletAmount: String? = null,
        val stakedAmount: String? = null,
        val totalAmount: String? = null,
        val transferAction: (() -> Unit)? = null,
        val nativeTokenName: String? = null,
        val nativeTokenLogoUrl: String? = null,
        val onTapAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                walletAmount = "20.00",
                stakedAmount = "30.00",
                totalAmount = "50.00",
                nativeTokenName = "DYDX",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxProfileBalancesViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier
//                .clickable {
//                    state.onTapAction?.invoke()
//                }
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(vertical = ThemeShapes.VerticalPadding),
        ) {
            CreateHeader(Modifier.padding(horizontal = ThemeShapes.HorizontalPadding), state)

            Row(
                modifier = modifier
                    .padding(vertical = ThemeShapes.VerticalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CreateAmountPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    title = state.localizer.localize("APP.GENERAL.WALLET"),
                    amount = state.walletAmount ?: "-",
                )

                Spacer(modifier = Modifier.width(16.dp))

                CreateAmountPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    title = state.localizer.localize("APP.GENERAL.STAKED"),
                    amount = state.stakedAmount ?: "-",
                )
            }

            CreateFooter(Modifier.padding(horizontal = ThemeShapes.HorizontalPadding), state)
        }
    }

    @Composable
    private fun CreateHeader(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier.padding(ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (state.nativeTokenLogoUrl != null) {
                PlatformImage(
                    icon = state.nativeTokenLogoUrl,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = state.nativeTokenName ?: "",
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_primary)
                    .themeFont(fontSize = ThemeFont.FontSize.base),
                modifier = Modifier.weight(1f),
            )

            if (state.transferAction != null) {
            }
        }
    }

    @Composable
    private fun CreateAmountPanel(modifier: Modifier, title: String, amount: String) {
        Column(
            modifier = modifier
                .background(
                    color = ThemeColor.SemanticColor.layer_4.color,
                    shape = RoundedCornerShape(10.dp),
                )
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_primary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = amount,
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_primary)
                    .themeFont(
                        fontType = ThemeFont.FontType.number,
                        fontSize = ThemeFont.FontSize.extra,
                    ),
            )
        }
    }

    @Composable
    private fun CreateFooter(modifier: Modifier, state: ViewState) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = modifier.padding(ThemeShapes.VerticalPadding),
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.TOTAL_BALANCE"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_secondary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
                modifier = Modifier.weight(1f),
            )

            Text(
                text = state.totalAmount ?: "-",
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_primary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = state.nativeTokenName ?: "",
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_secondary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )
        }
    }
}
