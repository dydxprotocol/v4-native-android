package exchange.dydx.trading.feature.shared.viewstate

import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LeverageRiskView
import exchange.dydx.trading.feature.shared.views.MarginUsageView
import exchange.dydx.utilities.utils.NumericFilter
import exchange.dydx.utilities.utils.filter

data class SharedAccountViewState(
    val buyingPower: String?,
    val marginUsage: String?,
    val marginUsageIcon: MarginUsageView.ViewState?,
    val equity: String?,
    val freeCollateral: String?,
    val openInterest: String?,
    val leverage: String?,
    val leverageIcon: LeverageRiskView.ViewState?,
) {
    companion object {
        val preview = SharedAccountViewState(
            buyingPower = "$22,222.12",
            marginUsage = "4.55%",
            marginUsageIcon = MarginUsageView.ViewState.preview,
            equity = "$22,222.12",
            freeCollateral = "$22,222.12",
            openInterest = "$22,222.12",
            leverage = "0.12x",
            leverageIcon = LeverageRiskView.ViewState.preview,
        )

        fun create(
            subaccount: Subaccount?,
            localizer: LocalizerProtocol,
            formatter: DydxFormatter,
        ): SharedAccountViewState? {
            if (subaccount == null) {
                return null
            }

            val margin = subaccount.marginUsage?.current

            return SharedAccountViewState(
                freeCollateral = formatter.dollar(
                    number = subaccount.freeCollateral?.current,
                    digits = 2,
                ),
                buyingPower = formatter.dollar(
                    number = subaccount.buyingPower?.current?.filter(
                        NumericFilter.NotNegative,
                    ),
                    digits = 2,
                ),
                marginUsage = formatter.percent(
                    number = subaccount.marginUsage?.current,
                    digits = 2,
                ),
                leverage = formatter.leverage(number = subaccount.leverage?.current),
                equity = formatter.dollar(number = subaccount.equity?.current, digits = 2),
                openInterest = formatter.dollarVolume(
                    number = subaccount.notionalTotal?.current,
                    digits = 2,
                ),
                leverageIcon = if (margin != null) {
                    LeverageRiskView.ViewState(
                        localizer = localizer,
                        level = LeverageRiskView.Level.createFromMarginUsage(margin),
                    )
                } else {
                    null
                },
                marginUsageIcon = if (margin != null) {
                    MarginUsageView.ViewState(
                        localizer = localizer,
                        percent = margin,
                    )
                } else {
                    null
                },
            )
        }
    }
}
