package exchange.dydx.trading.feature.vault.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.SignedAmountView

@Preview
@Composable
fun Preview_DydxVaultChartSelectedInfoView() {
    DydxThemedPreviewSurface {
        DydxVaultChartSelectedInfoView.Content(Modifier, DydxVaultChartSelectedInfoView.ViewState.preview)
    }
}

object DydxVaultChartSelectedInfoView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val change: SignedAmountView.ViewState? = null,
        val currentValue: String? = null,
        val entryDate: String? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                change = SignedAmountView.ViewState.preview,
                currentValue = "$1.0M",
                entryDate = "2021-01-01",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultChartSelectedInfoViewModel = hiltViewModel()

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
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            Text(
                text = state.entryDate ?: "",
                modifier = Modifier,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.base)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
            ) {
                Text(
                    text = state.currentValue ?: "-",
                    modifier = modifier,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                SignedAmountView.Content(
                    modifier = modifier,
                    state = state.change,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
