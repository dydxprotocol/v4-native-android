package exchange.dydx.trading.feature.vault.depositwithdraw.confirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.validation.DydxValidationView
import exchange.dydx.trading.feature.shared.views.HeaderView
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.vault.depositwithdraw.components.VaultAmountBox
import exchange.dydx.trading.feature.vault.receipt.DydxVaultReceiptView

@Preview
@Composable
fun Preview_DydxVaultConfirmationView() {
    DydxThemedPreviewSurface {
        DydxVaultConfirmationView.Content(Modifier, DydxVaultConfirmationView.ViewState.preview)
    }
}

object DydxVaultConfirmationView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val headerTitle: String? = null,
        val sourceLabel: String? = null,
        val sourceValue: String? = null,
        val destinationValue: String? = null,
        val ctaButton: InputCtaButton.ViewState? = null,
        val backAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                headerTitle = "Confirm Deposit",
                sourceLabel = "Amount to deposit",
                sourceValue = "$1,000.00",
                destinationValue = "Vault",
                ctaButton = InputCtaButton.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultConfirmationViewModel = hiltViewModel()

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
                .fillMaxSize(),
        ) {
            HeaderView(
                title = state.headerTitle ?: "",
                backAction = state.backAction,
            )

            PlatformDivider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
//                item {
//                    VaultAmountBox.Content(
//                        modifier = Modifier.animateItemPlacement(),
//                        state = state.transferAmount,
//                    )
//                }
//
//                item {
//                    DydxValidationView.Content(Modifier.animateItemPlacement())
//                }
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

