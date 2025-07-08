package exchange.dydx.trading.feature.portfolio.cancelpendingposition

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.receipt.components.buyingpower.DydxReceiptFreeCollateralView
import exchange.dydx.trading.feature.receipt.components.isolatedmargin.DydxReceiptIsolatedPositionMarginUsageView
import exchange.dydx.trading.feature.receipt.components.ordercount.DydxReceiptOrderCountView
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton

@Preview
@Composable
fun Preview_DydxCancelPendingPositionView() {
    DydxThemedPreviewSurface {
        DydxCancelPendingPositionView.Content(
            Modifier,
            DydxCancelPendingPositionView.ViewState.preview,
        )
    }
}

object DydxCancelPendingPositionView : DydxComponent {
    enum class CtaButtonState {
        Enabled,
        Disabled,
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val logoUrl: String?,
        val text: String,
        val orderCount: DydxReceiptOrderCountView.ViewState,
        val freeCollateral: DydxReceiptFreeCollateralView.ViewState,
        val marginUsage: DydxReceiptIsolatedPositionMarginUsageView.ViewState,
        val closeAction: () -> Unit = {},
        val cancelAction: () -> Unit = {},
        val ctaButtonState: CtaButtonState = CtaButtonState.Enabled,
        val ctaButtonTitle: String? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                logoUrl = "https://dydx.exchange/images/tokens/ETH.svg",
                text = "Cancel Pending Position",
                orderCount = DydxReceiptOrderCountView.ViewState.preview,
                freeCollateral = DydxReceiptFreeCollateralView.ViewState.preview,
                marginUsage = DydxReceiptIsolatedPositionMarginUsageView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxCancelPendingPositionViewModel = hiltViewModel()

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
                .background(ThemeColor.SemanticColor.layer_2.color)
                .fillMaxSize(),
        ) {
            HeaderView(
                modifier = Modifier,
                state = state,
            )

            PlatformDivider()

            Text(
                text = state.text,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.medium),
                modifier = Modifier.padding(ThemeShapes.VerticalPadding * 2),
            )

            Spacer(modifier = Modifier.weight(1f))

            ReceiptAndButton(
                modifier = Modifier,
                state = state,
            )
        }
    }

    @Composable
    private fun HeaderView(
        modifier: Modifier = Modifier,
        state: ViewState,
    ) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(
                    vertical = ThemeShapes.VerticalPadding,
                )
                .padding(top = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))

            PlatformRoundImage(
                icon = state.logoUrl,
                size = 40.dp,
            )

            Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))

            Text(
                text = state.localizer.localize("APP.TRADE.CANCEL_ORDERS"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.extra, fontType = ThemeFont.FontType.plus),
            )

            Spacer(modifier = Modifier.weight(1f))

            HeaderViewCloseBotton(closeAction = state.closeAction)
        }
    }

    @Composable
    private fun ReceiptAndButton(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                ),
        ) {
            val shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp)
            ReceiptView(
                modifier = Modifier
                    .offset(y = 12.dp)
                    .background(color = ThemeColor.SemanticColor.layer_0.color, shape = shape)
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = 12.dp)
                    .padding(bottom = 12.dp),
                state = state,
            )

            PlatformButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                text = state.ctaButtonTitle ?: "",
                state = when (state.ctaButtonState) {
                    CtaButtonState.Enabled -> PlatformButtonState.Destructive
                    CtaButtonState.Disabled -> PlatformButtonState.Disabled
                },
            ) {
                state.cancelAction.invoke()
            }
        }
    }

    @Composable
    private fun ReceiptView(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            DydxReceiptOrderCountView.Content(
                modifier = Modifier,
                state = state.orderCount,
            )

            DydxReceiptIsolatedPositionMarginUsageView.Content(
                modifier = Modifier,
                state = state.marginUsage,
            )

            DydxReceiptFreeCollateralView.Content(
                modifier = Modifier,
                state = state.freeCollateral,
            )
        }
    }
}
