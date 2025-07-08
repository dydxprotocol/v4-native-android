package exchange.dydx.trading.feature.vault.depositwithdraw

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton
import exchange.dydx.trading.feature.vault.depositwithdraw.deposit.DydxVaultDepositView
import exchange.dydx.trading.feature.vault.depositwithdraw.withdraw.DydxVaultWithdrawView

@Preview
@Composable
fun Preview_dydxVaultDepositWithdrawView() {
    DydxThemedPreviewSurface {
        DydxVaultDepositWithdrawView.Content(
            Modifier,
            DydxVaultDepositWithdrawView.ViewState.preview,
        )
    }
}

object DydxVaultDepositWithdrawView : DydxComponent {
    enum class DepositWithdrawType {
        DEPOSIT,
        WITHDRAW
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val closeAction: (() -> Unit)? = null,
        val selection: DydxVaultDepositWithdrawSelectionView.Selection? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        Content(modifier, type = DepositWithdrawType.DEPOSIT)
    }

    @Composable
    fun Content(modifier: Modifier, type: DepositWithdrawType) {
        val viewModel: DydxVaultDepositWithdrawViewModel = hiltViewModel()
        LaunchedEffect(Unit) {
            viewModel.type = type
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
            modifier = modifier
                .animateContentSize()
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_3),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = ThemeShapes.VerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                DydxVaultDepositWithdrawSelectionView.Content(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = ThemeShapes.VerticalPadding),
                )

                HeaderViewCloseBotton(
                    closeAction = state.closeAction,
                )
            }

            PlatformDivider()

            when (state.selection) {
                DydxVaultDepositWithdrawSelectionView.Selection.Deposit -> {
                    DydxVaultDepositView.Content(
                        modifier = Modifier,
                    )
                }
                DydxVaultDepositWithdrawSelectionView.Selection.Withdrawal -> {
                    DydxVaultWithdrawView.Content(
                        modifier = Modifier,
                    )
                }

                null -> {
                    // Do nothing
                }
            }
        }
    }
}
