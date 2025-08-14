package exchange.dydx.trading.feature.transfer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import exchange.dydx.trading.common.navigation.DydxAnimation
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton
import exchange.dydx.trading.feature.transfer.deposit.DydxTransferInstantDepositView
import exchange.dydx.trading.feature.transfer.faucet.DydxTransferFaucetView
import exchange.dydx.trading.feature.transfer.transferout.DydxTransferOutView
import exchange.dydx.trading.feature.transfer.withdrawal.DydxTransferWithdrawalView

@Preview
@Composable
fun Preview_DydxTransferView() {
    DydxThemedPreviewSurface {
        DydxTransferView.Content(Modifier, DydxTransferView.ViewState.preview)
    }
}

object DydxTransferView : DydxComponent {

    data class ViewState(
        val localizer: LocalizerProtocol,
        val closeAction: (() -> Unit)? = null,
        val selection: DydxTransferSectionsView.Selection? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferViewModel = hiltViewModel()

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
                .animateContentSize()
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_3),
        ) {
            Row(
                modifier
                    .fillMaxWidth()
                    .padding(vertical = ThemeShapes.VerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                DydxTransferSectionsView.Content(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = ThemeShapes.VerticalPadding),
                )

                HeaderViewCloseBotton(
                    closeAction = state.closeAction,
                )
            }

            PlatformDivider()

            DydxAnimation.AnimateFadeInOut(
                visible = state.selection == DydxTransferSectionsView.Selection.Deposit,
            ) {
                DydxTransferInstantDepositView.Content(
                    modifier = Modifier,
                )
            }
            DydxAnimation.AnimateFadeInOut(
                visible = state.selection == DydxTransferSectionsView.Selection.Withdrawal,
            ) {
                DydxTransferWithdrawalView.Content(
                    modifier = Modifier,
                )
            }
            DydxAnimation.AnimateFadeInOut(
                visible = state.selection == DydxTransferSectionsView.Selection.TransferOut,
            ) {
                DydxTransferOutView.Content(
                    modifier = Modifier,
                )
            }
            DydxAnimation.AnimateFadeInOut(
                visible = state.selection == DydxTransferSectionsView.Selection.Faucet,
            ) {
                DydxTransferFaucetView.Content(
                    modifier = Modifier,
                )
            }
            DydxAnimation.AnimateFadeInOut(
                visible = state.selection == null,
            ) {
                Spacer(modifier = Modifier)
            }
        }
    }
}
