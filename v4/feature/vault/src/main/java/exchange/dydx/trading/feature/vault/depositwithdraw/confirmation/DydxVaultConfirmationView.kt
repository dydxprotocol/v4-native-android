package exchange.dydx.trading.feature.vault.depositwithdraw.confirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformImage
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
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.HeaderView
import exchange.dydx.trading.feature.shared.views.InputCtaButton
import exchange.dydx.trading.feature.vault.depositwithdraw.components.VaultSlippageCheckbox
import exchange.dydx.trading.feature.vault.depositwithdraw.components.VaultTosCheckbox
import exchange.dydx.trading.feature.vault.receipt.DydxVaultReceiptView

@Preview
@Composable
fun Preview_DydxVaultConfirmationView() {
    DydxThemedPreviewSurface {
        DydxVaultConfirmationView.Content(Modifier, DydxVaultConfirmationView.ViewState.preview)
    }
}

object DydxVaultConfirmationView : DydxComponent {
    enum class Direction {
        Deposit,
        Withdraw
    }
    data class ViewState(
        val localizer: LocalizerProtocol,
        val headerTitle: String? = null,
        val sourceLabel: String? = null,
        val sourceValue: String? = null,
        val destinationValue: String? = null,
        val destinationIcon: Any? = null,
        val ctaButton: InputCtaButton.ViewState? = null,
        val backAction: (() -> Unit)? = null,
        val direction: Direction? = null,
        val slippage: VaultSlippageCheckbox.ViewState? = null,
        val tos: VaultTosCheckbox.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                headerTitle = "Confirm Deposit",
                sourceLabel = "Amount to deposit",
                sourceValue = "$1,000.00",
                destinationValue = "Vault",
                ctaButton = InputCtaButton.ViewState.preview,
                direction = Direction.Deposit,
                slippage = VaultSlippageCheckbox.ViewState.preview,
                tos = VaultTosCheckbox.ViewState.preview,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                ItemContent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(ThemeShapes.HorizontalPadding),
                    label = state.sourceLabel,
                    value = state.sourceValue,
                    icon = R.drawable.vault_usdc_token,
                )

                PlatformImage(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(28.dp),
                    icon = R.drawable.icon_right_arrow_2,
                )

                ItemContent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(ThemeShapes.HorizontalPadding),
                    label = state.localizer.localize("APP.GENERAL.DESTINATION"),
                    value = state.destinationValue,
                    icon = state.destinationIcon,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            DydxVaultReceiptView.Content(
                modifier = Modifier.offset(y = ThemeShapes.VerticalPadding),
            )

            if (state.slippage != null) {
                VaultSlippageCheckbox.Content(
                    modifier = Modifier.padding(ThemeShapes.HorizontalPadding),
                    state = state.slippage,
                )
            }

            if (state.tos != null) {
                VaultTosCheckbox.Content(
                    modifier = Modifier.padding(ThemeShapes.HorizontalPadding),
                    state = state.tos,
                )
            }

            InputCtaButton.Content(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(bottom = ThemeShapes.VerticalPadding * 2),
                state = state.ctaButton,
            )
        }
    }

    @Composable
    private fun ItemContent(modifier: Modifier, label: String?, value: String?, icon: Any?) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            Text(
                text = label ?: "-",
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            val shape = RoundedCornerShape(10.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = ThemeColor.SemanticColor.layer_4.color, shape = shape)
                    .clip(shape)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
            ) {
                PlatformImage(
                    modifier = Modifier
                        .size(32.dp),
                    icon = icon,
                )
                Text(
                    text = value ?: "-",
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
            }
        }
    }
}
