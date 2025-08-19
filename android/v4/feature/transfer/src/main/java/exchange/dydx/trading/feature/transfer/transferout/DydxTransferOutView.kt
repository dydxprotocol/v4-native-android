package exchange.dydx.trading.feature.transfer.transferout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.DydxReceiptView
import exchange.dydx.trading.feature.receipt.validation.DydxValidationView
import exchange.dydx.trading.feature.shared.views.HeaderView
import exchange.dydx.trading.feature.transfer.components.AddressInputBox
import exchange.dydx.trading.feature.transfer.components.ChainsComboBox
import exchange.dydx.trading.feature.transfer.components.TokensComboBox
import exchange.dydx.trading.feature.transfer.components.TransferAmountBox
import exchange.dydx.trading.feature.transfer.components.TransferMemoBox

@Preview
@Composable
fun Preview_DydxTransferOutView() {
    DydxThemedPreviewSurface {
        DydxTransferOutView.Content(Modifier, DydxTransferOutView.ViewState.preview)
    }
}

object DydxTransferOutView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val addressInput: AddressInputBox.ViewState? = null,
        val chainsComboBox: ChainsComboBox.ViewState? = null,
        val tokensComboBox: TokensComboBox.ViewState? = null,
        val transferAmount: TransferAmountBox.ViewState? = null,
        val transferMemo: TransferMemoBox.ViewState? = null,
        val closeAction: (() -> Unit)? = null,
    ) {

        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                addressInput = AddressInputBox.ViewState.preview,
                chainsComboBox = ChainsComboBox.ViewState.preview,
                tokensComboBox = TokensComboBox.ViewState.preview,
                transferAmount = TransferAmountBox.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferOutViewModel = hiltViewModel()

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
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            if (state.closeAction != null) {
                HeaderView(
                    title = state.localizer.localize("APP.GENERAL.TRANSFER_OUT"),
                    closeAction = { state.closeAction.invoke() },
                )

                PlatformDivider()
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Row(Modifier.animateItemPlacement()) {
                        AddressInputBox.Content(
                            modifier = Modifier.weight(1f),
                            state = state.addressInput,
                        )
                        Spacer(Modifier.width(ThemeShapes.HorizontalPadding))
                        ChainsComboBox.Content(
                            modifier = Modifier.weight(1f),
                            state = state.chainsComboBox,
                        )
                    }
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
                    TransferMemoBox.Content(
                        modifier = Modifier.animateItemPlacement(),
                        state = state.transferMemo,
                    )
                }

                item {
                    DydxValidationView.Content(Modifier.animateItemPlacement())
                }
            }

            DydxReceiptView.Content(
                modifier = Modifier.offset(y = ThemeShapes.VerticalPadding),
            )
            DydxTransferOutCtaButton.Content(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(bottom = ThemeShapes.VerticalPadding * 2),
            )
        }
    }
}
