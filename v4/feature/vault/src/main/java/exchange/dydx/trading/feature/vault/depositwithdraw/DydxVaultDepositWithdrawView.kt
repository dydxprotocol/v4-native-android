package exchange.dydx.trading.feature.vault.depositwithdraw

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

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
        Content(modifier, type = DepositWithdrawType.DEPOSIT)
    }

    @Composable
    fun Content(modifier: Modifier, type: DepositWithdrawType) {
        val viewModel: DydxVaultDepositWithdrawViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
        Text(text = state?.text ?: "")
    }
}
