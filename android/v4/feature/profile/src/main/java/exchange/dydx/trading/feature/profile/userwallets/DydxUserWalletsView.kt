package exchange.dydx.trading.feature.profile.userwallets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_DydxUserWalletsView() {
    DydxThemedPreviewSurface {
        DydxUserWalletsView.Content(Modifier, DydxUserWalletsView.ViewState.preview)
    }
}

object DydxUserWalletsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val closeAction: (() -> Unit)? = null,
        val addWalletAction: (() -> Unit)? = null,
        val wallets: List<DydxUserWalletItemView.ViewState> = emptyList(),
        val onWalletSelected: ((Int) -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                wallets = listOf(
                    DydxUserWalletItemView.ViewState.preview,
                    DydxUserWalletItemView.ViewState.preview,
                    DydxUserWalletItemView.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxUserWalletsViewModel = hiltViewModel()

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
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.GENERAL.MANAGE_WALLET"),
                closeAction = { state.closeAction?.invoke() },
            )

            Spacer(modifier = Modifier.size(ThemeShapes.VerticalPadding))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
            ) {
                itemsIndexed(state.wallets) { index, wallet, ->
                    DydxUserWalletItemView.Content(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { state.onWalletSelected?.invoke(index) },
                        state = wallet,
                    )
                }
            }

            PlatformButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = ThemeShapes.VerticalPadding),
                text = state.localizer.localize("APP.GENERAL.ADD_NEW_WALLET"),
                action = { state.addWalletAction?.invoke() },
            )
        }
    }
}
