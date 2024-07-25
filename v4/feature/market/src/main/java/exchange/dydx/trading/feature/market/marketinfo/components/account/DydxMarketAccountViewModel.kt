package exchange.dydx.trading.feature.market.marketinfo.components.account

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.market.marketinfo.components.diff.DydxDiffView
import exchange.dydx.trading.feature.shared.views.MarginUsageView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxMarketAccountViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {
    enum class NumericFormat {
        DOLLAR,
        PERCENT,
        LEVERAGE,
    }

    val state: Flow<DydxMarketAccountView.ViewState?> = abacusStateManager.state.selectedSubaccount
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(subaccount: Subaccount?): DydxMarketAccountView.ViewState {
        return DydxMarketAccountView.ViewState(
            localizer = localizer,

            buyingPower = createTextDiffState(
                titleStringKey = localizer.localize("APP.GENERAL.BUYING_POWER"),
                format = NumericFormat.DOLLAR,
                diff = subaccount?.buyingPower,
            ),
            marginUsage = createMarginUsageDiffState(
                titleStringKey = localizer.localize("APP.GENERAL.MARGIN_USAGE"),
                diff = subaccount?.marginUsage,
            ),
            equity = createTextDiffState(
                titleStringKey = localizer.localize("APP.GENERAL.EQUITY"),
                format = NumericFormat.DOLLAR,
                diff = subaccount?.equity,
            ),
            freeCollateral = createTextDiffState(
                titleStringKey = localizer.localize("APP.GENERAL.FREE_COLLATERAL"),
                format = NumericFormat.DOLLAR,
                diff = subaccount?.freeCollateral,
            ),
            openInterest = createTextDiffState(
                titleStringKey = localizer.localize("APP.TRADE.OPEN_INTEREST"),
                format = NumericFormat.DOLLAR,
                diff = subaccount?.notionalTotal,
            ),
            accountLeverage = createTextDiffState(
                titleStringKey = localizer.localize("APP.GENERAL.LEVERAGE"),
                format = NumericFormat.LEVERAGE,
                diff = subaccount?.leverage,
            ),
        )
    }

    private fun createMarginUsageDiffState(
        titleStringKey: String,
        diff: TradeStatesWithDoubleValues?
    ): DydxDiffView.ViewState {
        return DydxDiffView.ViewState(
            lableText = localizer.localize(titleStringKey),
            diff = DydxDiffView.DiffType.MarginUsage(
                state = DydxDiffView.DiffState(
                    current = diff?.current?.let {
                        MarginUsageView.ViewState(
                            localizer = localizer,
                            displayOption = MarginUsageView.DisplayOption.IconAndValue,
                            percent = it,
                        )
                    },
                    after = diff?.postOrder?.let {
                        MarginUsageView.ViewState(
                            localizer = localizer,
                            displayOption = MarginUsageView.DisplayOption.IconAndValue,
                            percent = it,
                        )
                    },
                ),
            ),
            formatter = formatter,
        )
    }

    private fun createTextDiffState(
        titleStringKey: String,
        format: NumericFormat,
        diff: TradeStatesWithDoubleValues?
    ): DydxDiffView.ViewState {
        return DydxDiffView.ViewState(
            lableText = localizer.localize(titleStringKey),
            diff = createTextDiffType(diff, format),
            formatter = formatter,
        )
    }

    private fun createTextDiffType(
        diff: TradeStatesWithDoubleValues?,
        format: NumericFormat,
        digits: Int? = null
    ): DydxDiffView.DiffType {
        val state = diff?.let {
            when (format) {
                NumericFormat.DOLLAR -> DydxDiffView.DiffState(
                    formatter.dollar(diff.current, digits ?: 2),
                    formatter.dollar(diff.postOrder, digits ?: 2),
                )
                NumericFormat.PERCENT -> DydxDiffView.DiffState(
                    formatter.percent(diff.current, digits ?: 4),
                    formatter.percent(diff.postOrder, digits ?: 4),
                )
                NumericFormat.LEVERAGE -> DydxDiffView.DiffState(
                    formatter.leverage(diff.current, digits ?: 2),
                    formatter.leverage(diff.postOrder, digits ?: 2),
                )
            }
        } ?: DydxDiffView.DiffState(
            null,
            null,
        )

        return DydxDiffView.DiffType.Text(state)
    }
}
