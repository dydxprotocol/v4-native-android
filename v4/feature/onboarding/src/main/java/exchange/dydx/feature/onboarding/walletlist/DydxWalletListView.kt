package exchange.dydx.feature.onboarding.walletlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.feature.onboarding.walletlist.components.DydxWalletListItemView
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
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_DydxWalletListView() {
    DydxThemedPreviewSurface {
        DydxWalletListView.Content(Modifier, DydxWalletListView.ViewState.preview)
    }
}

object DydxWalletListView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val desktopSync: DydxWalletListItemView.ViewState? = null,
        val debugScan: DydxWalletListItemView.ViewState? = null,
        val wcModal: DydxWalletListItemView.ViewState? = null,
        val wallets: List<DydxWalletListItemView.ViewState> = emptyList(),
        val backButtonHandler: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                DydxWalletListItemView.ViewState.preview,
                DydxWalletListItemView.ViewState.preview,
                DydxWalletListItemView.ViewState.preview,
                listOf(DydxWalletListItemView.ViewState.preview),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxWalletListViewModel = hiltViewModel()
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            viewModel.updateContext(context)
        }
        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
        Column(
            modifier = Modifier
                .background(exchange.dydx.platformui.designSystem.theme.ThemeColor.SemanticColor.layer_2.color)
                .fillMaxSize(),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.ONBOARDING.SELECT_WALLET"),
                closeAction = { state.backButtonHandler.invoke() },
            )
            Text(
                text = state.localizer.localize("APP.ONBOARDING.SELECT_WALLET_TEXT"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                modifier = Modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(bottom = ThemeShapes.VerticalPadding),
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                state.desktopSync?.let { item ->
                    item(key = "desktop") {
                        DydxWalletListItemView(item).Content(Modifier)
                    }
                }

                state.debugScan?.let { item ->
                    item(key = "debug") {
                        DydxWalletListItemView(item).Content(Modifier)
                    }
                }

                if (state.desktopSync != null || state.debugScan != null) {
                    item(key = "spacer") { Spacer(modifier = Modifier.height(24.dp)) }
                }

                state.wcModal?.let { item ->
                    item(key = "wc") {
                        DydxWalletListItemView(item).Content(Modifier)
                    }
                    item(key = "spacer2") { Spacer(modifier = Modifier.height(24.dp)) }
                }

                state.wallets.let { wallets ->
                    items(items = wallets, key = { it.main }) { wallet ->
                        DydxWalletListItemView(wallet).Content(Modifier)
                    }
                }
            }

            BackHandler {
                state.backButtonHandler.invoke()
            }
        }
    }
}
