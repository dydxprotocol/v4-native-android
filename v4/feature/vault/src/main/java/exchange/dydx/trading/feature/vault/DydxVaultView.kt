package exchange.dydx.trading.feature.vault

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.bottombar.DydxBottomBarScaffold
import exchange.dydx.trading.feature.vault.components.DydxVaultButtonsView
import exchange.dydx.trading.feature.vault.components.DydxVaultHeaderView

@Preview
@Composable
fun Preview_DydxVaultView() {
    DydxThemedPreviewSurface {
        DydxVaultView.Content(Modifier, DydxVaultView.ViewState.preview)
    }
}

object DydxVaultView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val text: String?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                text = "1.0M",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        DydxBottomBarScaffold(Modifier) {
            Content(it, state)
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
                DydxVaultHeaderView.Content(modifier = Modifier)

                Spacer(modifier = Modifier.weight(1f))

                DydxVaultButtonsView.Content(modifier = Modifier)
        }
    }
}
