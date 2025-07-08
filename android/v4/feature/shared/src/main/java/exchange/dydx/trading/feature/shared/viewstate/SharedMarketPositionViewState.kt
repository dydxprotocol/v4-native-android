package exchange.dydx.trading.feature.shared.viewstate

import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.components.gradient.GradientType
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LeverageRiskView
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import kotlin.math.absoluteValue

data class SharedMarketPositionViewState(
    val id: String,
    val childSubaccountNumber: Int? = null, // null if it is cross margin in parent subaccount
    val marginMode: MarginMode? = null,
    val unrealizedPNLAmount: SignedAmountView.ViewState? = null,
    val unrealizedPNLPercent: SignedAmountView.ViewState? = null,
    val realizedPNLAmount: SignedAmountView.ViewState? = null,
    val leverage: String? = null,
    val leverageIcon: LeverageRiskView.ViewState? = null,
    val liquidationPrice: String? = null,
    val side: SideTextView.ViewState? = null,
    val size: String? = null,
    val notionalTotal: String? = null,
    val margin: String? = null,
    val token: TokenTextView.ViewState? = null,
    val logoUrl: String? = null,
    val gradientType: GradientType = GradientType.NONE,
    val oraclePrice: String? = null,
    val entryPrice: String? = null,
    val exitPrice: String? = null,
    val funding: SignedAmountView.ViewState? = null,
    val onAdjustMarginAction: (() -> Unit)? = null,
) {
    companion object {
        val preview = SharedMarketPositionViewState(
            id = "1",
            unrealizedPNLAmount = SignedAmountView.ViewState.preview,
            unrealizedPNLPercent = SignedAmountView.ViewState.preview,
            realizedPNLAmount = SignedAmountView.ViewState.preview,
            leverage = "$12.00",
            leverageIcon = LeverageRiskView.ViewState.preview,
            liquidationPrice = "$12.00",
            side = SideTextView.ViewState.preview,
            size = "0.0012",
            margin = "$120.00",
            token = TokenTextView.ViewState.preview,
            logoUrl = "https://media.dydx.exchange/currencies/eth.png",
            gradientType = GradientType.PLUS,
            oraclePrice = "$12.00",
            entryPrice = "$12.00",
            exitPrice = "$12.00",
            funding = SignedAmountView.ViewState.preview,
        )

        fun create(
            position: SubaccountPosition,
            market: PerpetualMarket,
            asset: Asset?,
            formatter: DydxFormatter,
            localizer: LocalizerProtocol,
            onAdjustMarginAction: (() -> Unit)
        ): SharedMarketPositionViewState? {
            val configs = market.configs ?: return null
            val positionSize = position.size.current ?: 0.0
            val notionalTotal = position.notionalTotal.current ?: 0.0
            if (positionSize == 0.0) return null

            val unrealizedPnlPercent = position.unrealizedPnlPercent.current ?: 0.0
            val unrealizedPnlSign: PlatformUISign = when {
                unrealizedPnlPercent > 0 -> PlatformUISign.Plus
                unrealizedPnlPercent < 0 -> PlatformUISign.Minus
                else -> PlatformUISign.None
            }

            val realizedPNLAmount = position.realizedPnl.current ?: 0.0
            val realizedPNLSign: PlatformUISign = when {
                realizedPNLAmount > 0 -> PlatformUISign.Plus
                realizedPNLAmount < 0 -> PlatformUISign.Minus
                else -> PlatformUISign.None
            }

            val netFunding = position.netFunding ?: 0.0
            val netFundingSign: PlatformUISign = when {
                netFunding > 0 -> PlatformUISign.Plus
                netFunding < 0 -> PlatformUISign.Minus
                else -> PlatformUISign.None
            }

            val leverage = position.leverage.current ?: 0.0
            val maxLeverage = position.maxLeverage.current ?: 0.0
            val riskLevel =
                if (leverage > 0 && maxLeverage > 0) {
                    LeverageRiskView.Level.createFromMarginUsage(
                        leverage / maxLeverage,
                    )
                } else {
                    null
                }
            return SharedMarketPositionViewState(
                id = position.id,
                childSubaccountNumber = position.childSubaccountNumber,
                marginMode = position.marginMode,
                size = formatter.localFormatted(
                    positionSize.absoluteValue,
                    configs.displayStepSizeDecimals ?: 1,
                ),
                notionalTotal = formatter.dollar(notionalTotal, 2),
                token = asset?.displayableAssetId?.let {
                    TokenTextView.ViewState(
                        symbol = it,
                    )
                },
                side = SideTextView.ViewState(
                    localizer = localizer,
                    coloringOption = SideTextView.ColoringOption.WITH_BACKGROUND,
                    side = if (position.resources.indicator.current == "long") SideTextView.Side.Long else SideTextView.Side.Short,
                ),
                gradientType = if (position.resources.indicator.current == "long") GradientType.PLUS else GradientType.MINUS,
                leverage = formatter.leverage(leverage, 2),
                leverageIcon = riskLevel?.let {
                    LeverageRiskView.ViewState(
                        localizer = localizer,
                        level = it,
                        viewSize = 18.dp,
                        displayOption = LeverageRiskView.DisplayOption.IconOnly,
                    )
                },
                unrealizedPNLPercent = SignedAmountView.ViewState(
                    text = formatter.percent(unrealizedPnlPercent.absoluteValue, 2),
                    sign = unrealizedPnlSign,
                    coloringOption = SignedAmountView.ColoringOption.SignOnly,
                ),
                unrealizedPNLAmount = SignedAmountView.ViewState(
                    text = formatter.dollar(
                        position.unrealizedPnl.current?.absoluteValue ?: 0.0,
                        2,
                    ),
                    sign = unrealizedPnlSign,
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                ),
                realizedPNLAmount = SignedAmountView.ViewState(
                    text = formatter.dollar(realizedPNLAmount.absoluteValue, 2),
                    sign = realizedPNLSign,
                    coloringOption = SignedAmountView.ColoringOption.AllText,
                ),
                logoUrl = asset?.resources?.imageUrl,
                oraclePrice = formatter.dollar(
                    market.oraclePrice,
                    configs.displayTickSizeDecimals ?: 0,
                ),
                entryPrice = formatter.dollar(
                    position.entryPrice.current,
                    configs.displayTickSizeDecimals ?: 0,
                ),
                exitPrice = formatter.dollar(
                    position.exitPrice,
                    configs.displayTickSizeDecimals ?: 0,
                ),
                liquidationPrice = formatter.dollar(
                    position.liquidationPrice.current,
                    configs.displayTickSizeDecimals ?: 0,
                ),
                margin = formatter.dollar(
                    position.marginValue.current,
                    2,
                ),
                funding = SignedAmountView.ViewState(
                    text = formatter.dollar(netFunding.absoluteValue),
                    sign = netFundingSign,
                    coloringOption = SignedAmountView.ColoringOption.SignOnly,
                ),
                onAdjustMarginAction = onAdjustMarginAction,
            )
        }
    }
}
