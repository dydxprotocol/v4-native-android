package exchange.dydx.trading.feature.transfer.selector

import android.R.attr.action
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
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

@Preview
@Composable
fun Preview_DydxTransferSelectorView() {
    DydxThemedPreviewSurface {
        DydxTransferSelectorView.Content(Modifier, DydxTransferSelectorView.ViewState.preview)
    }
}

object DydxTransferSelectorView : DydxComponent {
    enum class Action {
        Deposit, Withdrawal, TransferOut, Faucet;

        val titleKey: String
            get() = when (this) {
                Deposit -> "APP.GENERAL.DEPOSIT"
                Withdrawal -> "APP.GENERAL.WITHDRAW"
                TransferOut -> "APP.GENERAL.TRANSFER"
                Faucet -> "Faucet"
            }

        val subtitleKey: String
            get() = when (this) {
                Deposit -> "APP.ONBOARDING.DEPOSIT_DESC"
                Withdrawal -> "APP.ONBOARDING.WITHDRAWAL_DESC"
                TransferOut -> "APP.ONBOARDING.TRANSFEROUT_DESC"
                Faucet -> "Fund wallet with testnet USDC on dYdX chain"
            }

        val icon: Int
            get() = when (this) {
                Deposit -> R.drawable.icon_transfer_deposit_2
                Withdrawal -> R.drawable.icon_transfer_withdraw_2
                TransferOut -> R.drawable.icon_swap_vertical
                Faucet -> R.drawable.icon_transfer_deposit
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val onActionTapped: (Action) -> Unit = {},
        val isMainnet: Boolean = true,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferSelectorViewModel = hiltViewModel()

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
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.GENERAL.TRANSFERS"),
                closeAction = { state.closeAction?.invoke() },
            )

            PlatformDivider()

            Column(
                modifier = Modifier
                    .padding(top = ThemeShapes.VerticalPadding),
                verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
            ) {
                ButtonContent(action = Action.Deposit, state = state)
                ButtonContent(action = Action.Withdrawal, state = state)
                ButtonContent(action = Action.TransferOut, state = state)
                if (!state.isMainnet) {
                    ButtonContent(action = Action.Faucet, state = state)
                }
            }
        }
    }

    @Composable
    fun ButtonContent(modifier: Modifier = Modifier, action: Action, state: ViewState) {
        Row(
            modifier = modifier
                .clickable {
                    state.onActionTapped(action)
                }
                .padding(vertical = 4.dp)
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            Icon(
                painter = painterResource(id = action.icon),
                contentDescription = "",
                modifier = Modifier.size(18.dp),
                tint = ThemeColor.SemanticColor.text_primary.color,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.localizer.localize(action.titleKey),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                Text(
                    text = state.localizer.localize(action.subtitleKey),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.chevron_right),
                contentDescription = "",
                modifier = Modifier.size(16.dp),
                tint = ThemeColor.SemanticColor.text_secondary.color,
            )
        }
    }
}
