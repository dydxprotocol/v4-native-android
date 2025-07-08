package exchange.dydx.trading.feature.vault.depositwithdraw.deposit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.validation.DydxValidationView
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.vault.depositwithdraw.components.VaultAmountBox
import exchange.dydx.trading.feature.vault.receipt.DydxVaultReceiptView

@Preview
@Composable
fun Preview_DydxVaultDepositView() {
    DydxThemedPreviewSurface {
        DydxVaultDepositView.Content(Modifier, DydxVaultDepositView.ViewState.preview)
    }
}

object DydxVaultDepositView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val transferAmount: VaultAmountBox.ViewState? = null,
        val validation: DydxValidationView.ViewState? = null,
        val ctaButton: InputCtaButton.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                transferAmount = VaultAmountBox.ViewState.preview,
                validation = DydxValidationView.ViewState.preview,
                ctaButton = InputCtaButton.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultDepositViewModel = hiltViewModel()

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    VaultAmountBox.Content(
                        modifier = Modifier.animateItemPlacement(),
                        state = state.transferAmount,
                    )
                }

                if (state.validation != null) {
                    item {
                        DydxValidationView.Content(
                            modifier = Modifier.animateItemPlacement(),
                            state = state.validation,
                        )
                    }
                }
            }

            DydxVaultReceiptView.Content(
                modifier = Modifier.offset(y = ThemeShapes.VerticalPadding),
            )

            InputCtaButton.Content(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(bottom = ThemeShapes.VerticalPadding * 2),
                state = state.ctaButton,
            )
        }
    }
}
