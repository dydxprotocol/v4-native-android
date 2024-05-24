package exchange.dydx.trading.feature.portfolio.components.transfers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformRoundIcon
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.IntervalText
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import java.util.UUID

@Preview
@Composable
fun Preview_DydxPortfolioTransfersItemView() {
    DydxThemedPreviewSurface {
        DydxPortfolioTransfersItemView.Content(
            Modifier,
            DydxPortfolioTransfersItemView.ViewState.preview,
        )
    }
}

object DydxPortfolioTransfersItemView {
    enum class TransferType {
        Deposit, Withdrawal, TransferIn, TransferOut;

        val logo: Any
            get() =
                when (this) {
                    Deposit, TransferIn -> R.drawable.icon_transfer_deposit
                    Withdrawal, TransferOut -> R.drawable.icon_transfer_withdrawal
                }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val id: UUID = UUID.randomUUID(),
        val date: IntervalText.ViewState? = null,
        val type: TransferType = TransferType.Deposit,
        val amount: SignedAmountView.ViewState? = null,
        val address: String = "",
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                date = IntervalText.ViewState.preview,
                type = TransferType.Deposit,
                amount = SignedAmountView.ViewState.preview,
                address = "address",
            )
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (state.date != null) {
                    IntervalText.Content(
                        modifier = Modifier,
                        state = state.date,
                        textStyle = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                } else {
                    Text(
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center,
                        text = "-",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }
            }

            PlatformRoundIcon(
                icon = state.type.logo,
                size = 34.dp,
                iconSize = 26.dp,
                backgroundColor = ThemeColor.SemanticColor.layer_3,
                iconTint = ThemeColor.SemanticColor.text_secondary,
            )

            Text(
                text = when (state.type) {
                    TransferType.Deposit -> state.localizer.localize("APP.GENERAL.DEPOSIT")
                    TransferType.Withdrawal -> state.localizer.localize("APP.GENERAL.WITHDRAW")
                    TransferType.TransferIn -> state.localizer.localize("APP.GENERAL.TRANSFER_IN")
                    TransferType.TransferOut -> state.localizer.localize("APP.GENERAL.TRANSFER_OUT")
                },
                style = TextStyle.dydxDefault.themeFont(fontSize = ThemeFont.FontSize.medium),
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = state.address,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    textAlign = TextAlign.End,
                )

                SignedAmountView.Content(
                    modifier = Modifier.align(Alignment.End),
                    state = state.amount,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                )
            }
        }
    }
}
