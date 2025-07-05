package exchange.dydx.trading.feature.portfolio.components.fundings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.negativeColor
import exchange.dydx.platformui.designSystem.theme.positiveColor
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.IntervalText
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.TokenTextView

@Preview
@Composable
fun Preview_DydxPortfolioFundingItemView() {
    DydxThemedPreviewSurface {
        DydxPortfolioFundingItemView.Content(
            Modifier,
            DydxPortfolioFundingItemView.ViewState.preview,
        )
    }
}

object DydxPortfolioFundingItemView {
    enum class FundingStatus {
        paid,
        earned;

        fun directionText(localizer: LocalizerProtocol): String {
            return when (this) {
                earned -> localizer.localize("APP.GENERAL.FUNDING_EARNED")
                paid -> localizer.localize("APP.GENERAL.FUNDING_PAID")
            }
        }

        val templateColor: ThemeColor.SemanticColor
            get() = when (this) {
                earned -> ThemeColor.SemanticColor.positiveColor
                paid -> ThemeColor.SemanticColor.negativeColor
            }

        val statusIcon: Any
            get() = when (this) {
                earned -> R.drawable.icon_funding_earned
                paid -> R.drawable.icon_funding_paid
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val id: String? = null,
        val amount: SignedAmountView.ViewState? = null,
        val rate: SignedAmountView.ViewState? = null,
        val date: IntervalText.ViewState? = null,
        val sideText: SideTextView.ViewState? = null,
        val status: FundingStatus = FundingStatus.paid,
        val position: String? = null,
        val token: TokenTextView.ViewState? = null,
        val logoUrl: String? = null,

    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                id = "1.0M",
                amount = SignedAmountView.ViewState.preview,
                rate = SignedAmountView.ViewState.preview,
                date = IntervalText.ViewState.preview,
                sideText = SideTextView.ViewState.preview,
                position = "$1.00",
                token = TokenTextView.ViewState.preview,
                logoUrl = "https://media.dydx.exchange/currencies/eth.png",
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

            Box() {
                PlatformRoundImage(
                    icon = state.logoUrl,
                    size = 32.dp,
                )

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .offset(
                            x = 22.dp,
                            y = (-4).dp,
                        )
                        .clip(CircleShape)
                        .themeColor(background = ThemeColor.SemanticColor.layer_1),
                    contentAlignment = Alignment.Center,
                ) {
                    PlatformImage(
                        modifier = Modifier.size(10.dp),
                        icon = state.status.statusIcon,
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    modifier = Modifier,
                    text = state.status.directionText(state.localizer),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SideTextView.Content(
                        modifier = Modifier,
                        state = state.sideText,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini),
                    )

                    Text(
                        text = state.position ?: "",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_tertiary)
                            .themeFont(fontSize = ThemeFont.FontSize.mini),
                    )

                    TokenTextView.Content(
                        modifier = Modifier,
                        state = state.token,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.tiny),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    SignedAmountView.Content(
                        modifier = Modifier,
                        state = state.amount,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }

                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    SignedAmountView.Content(
                        modifier = Modifier,
                        state = state.rate,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini),
                    )
                }
            }
        }
    }
}

val SubaccountFundingPayment.id: String
    get() = "$marketId-$payment-$createdAtMilliseconds"
