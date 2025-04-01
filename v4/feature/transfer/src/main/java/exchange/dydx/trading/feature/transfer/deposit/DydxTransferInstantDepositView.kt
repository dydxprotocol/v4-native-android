package exchange.dydx.trading.feature.transfer.deposit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.DydxReceiptView
import exchange.dydx.trading.feature.receipt.validation.DydxValidationView
import exchange.dydx.trading.feature.transfer.components.InstantInputBox
import exchange.dydx.trading.feature.transfer.components.InstantSelector

@Preview
@Composable
fun Preview_DydxTransferInstantDepositView() {
    DydxThemedPreviewSurface {
        DydxTransferInstantDepositView.Content(
            Modifier,
            DydxTransferInstantDepositView.ViewState.preview,
        )
    }
}

object DydxTransferInstantDepositView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val inputBox: InstantInputBox.ViewState? = null,
        val selector: InstantSelector.ViewState? = null,
        val connectWalletAction: () -> Unit = {},
        val showConnectWallet: Boolean = false,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                inputBox = InstantInputBox.ViewState.preview,
                selector = InstantSelector.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferInstantDepositViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.showConnectWallet) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Text(
                                text = state.localizer.localize("APP.V4_DEPOSIT.MOBILE_WALLET_REQUIRED"),
                                style = TextStyle.dydxDefault,
                                modifier = Modifier.weight(1f),
                            )
                            PlatformButton(
                                text = state.localizer.localize("APP.GENERAL.CONNECT_WALLET"),
                            ) {
                                state.connectWalletAction()
                            }
                        }
                    }
                } else {
                    item {
                        InstantInputBox.Content(
                            modifier = Modifier,
                            state = state.inputBox,
                        )
                    }
                    item {
                        InstantSelector.Content(
                            modifier = Modifier,
                            state = state.selector,
                        )
                    }
                    item {
                        DydxValidationView.Content(Modifier)
                    }
                }
            }

            DydxReceiptView.Content(
                modifier = Modifier.offset(y = ThemeShapes.VerticalPadding),
            )
            DydxTransferDepositCtaButton.Content(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(bottom = ThemeShapes.VerticalPadding * 2),
            )
        }
    }
}
