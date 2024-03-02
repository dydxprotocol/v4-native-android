package exchange.dydx.trading.feature.transfer.deposit

import androidx.compose.foundation.ExperimentalFoundationApi
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
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.receipt.DydxReceiptView
import exchange.dydx.trading.feature.receipt.validation.DydxValidationView
import exchange.dydx.trading.feature.transfer.components.ChainsComboBox
import exchange.dydx.trading.feature.transfer.components.TokensComboBox
import exchange.dydx.trading.feature.transfer.components.TransferAmountBox

@Preview
@Composable
fun Preview_DydxTransferDepositView() {
    DydxThemedPreviewSurface {
        DydxTransferDepositView.Content(Modifier, DydxTransferDepositView.ViewState.preview)
    }
}

object DydxTransferDepositView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val chainsComboBox: ChainsComboBox.ViewState? = null,
        val tokensComboBox: TokensComboBox.ViewState? = null,
        val transferAmount: TransferAmountBox.ViewState? = null,
        val connectWalletAction: () -> Unit = {},
        val showConnectWallet: Boolean = false,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                chainsComboBox = ChainsComboBox.ViewState.preview,
                tokensComboBox = TokensComboBox.ViewState.preview,
                transferAmount = TransferAmountBox.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferDepositViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .fillMaxSize(),
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
                        ChainsComboBox.Content(
                            modifier = Modifier.animateItemPlacement(),
                            state = state.chainsComboBox,
                        )
                    }

                    item {
                        TokensComboBox.Content(
                            modifier = Modifier.animateItemPlacement(),
                            state = state.tokensComboBox,
                        )
                    }

                    item {
                        TransferAmountBox.Content(
                            modifier = Modifier.animateItemPlacement(),
                            state = state.transferAmount,
                        )
                    }

                    item {
                        DydxValidationView.Content(Modifier.animateItemPlacement())
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
