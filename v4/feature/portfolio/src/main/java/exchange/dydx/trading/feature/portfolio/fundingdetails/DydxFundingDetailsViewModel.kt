package exchange.dydx.trading.feature.portfolio.fundingdetails

import android.R.attr.digits
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.portfolio.components.fundings.DydxPortfolioFundingItemView
import exchange.dydx.trading.feature.portfolio.components.fundings.id
import exchange.dydx.trading.feature.shared.views.TokenTextView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.text.Typography.dollar
@HiltViewModel
class DydxFundingDetailsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    savedStateHandle: SavedStateHandle,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    private val id: String? = savedStateHandle["id"]

    val state: Flow<DydxFundingDetailsView.ViewState?> = combine(
        abacusStateManager.marketId,
        abacusStateManager.state.selectedSubaccountFundings,
        abacusStateManager.state.marketMap,
        abacusStateManager.state.assetMap,
    ) { marketId, fundings, marketMap, assetMap, ->
        createViewState(marketId, fundings, marketMap, assetMap)
    }
        .distinctUntilChanged()

    private fun createViewState(
        marketId: String?,
        fundings: List<SubaccountFundingPayment>?,
        marketMap: Map<String, PerpetualMarket>?,
        assetMap: Map<String, Asset>?,
    ): DydxFundingDetailsView.ViewState? {
        val funding = fundings?.first { it.id == id } ?: return null

        val market = marketMap?.get(funding.marketId) ?: return null
        val asset = assetMap?.get(market.assetId) ?: return null

        val longValue = funding.createdAtMilliseconds.toLong()
        val createdAt = formatter.dateTime(Instant.ofEpochMilli(longValue))

        val amount = formatter.dollar(funding.payment, digits = 4)
        val rate = formatter.percent(funding.rate, digits = 6)
        val status: DydxPortfolioFundingItemView.FundingStatus
        if (funding.payment >= 0.0) {
            status = DydxPortfolioFundingItemView.FundingStatus.earned
        } else {
            status = DydxPortfolioFundingItemView.FundingStatus.paid
        }

        val stepSize = market.configs?.displayStepSizeDecimals ?: 1
        val positionSize = formatter.raw(funding.positionSize.absoluteValue, digits = stepSize)

        val tickSize = market.configs?.displayTickSizeDecimals ?: 2
        val price = formatter.dollar(funding.price, digits = tickSize)

        return DydxFundingDetailsView.ViewState(
            localizer = localizer,
            logoUrl = asset.resources?.imageUrl,
            status = status,
            closeAction = {
                router.navigateBack()
            },
            items = listOf(
                DydxFundingDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.MARKET"),
                    value = DydxFundingDetailsView.Item.ItemValue.Any {
                        TokenTextView.Content(
                            modifier = Modifier,
                            state = TokenTextView.ViewState(symbol = asset.displayableAssetId),
                        )
                    },
                ),
                DydxFundingDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.AMOUNT"),
                    value = DydxFundingDetailsView.Item.ItemValue.Number(
                        amount,
                    ),
                ),
                DydxFundingDetailsView.Item(
                    title = localizer.localize("APP.TRADE.RATE"),
                    value = DydxFundingDetailsView.Item.ItemValue.Number(
                        rate,
                    ),
                ),
                DydxFundingDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.SIZE"),
                    value = DydxFundingDetailsView.Item.ItemValue.Number(
                        positionSize,
                    ),
                ),
                DydxFundingDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.SIDE"),
                    value = DydxFundingDetailsView.Item.ItemValue.StringValue(
                        if (funding.positionSize > 0.0) {
                            localizer.localize("APP.GENERAL.BUY")
                        } else {
                            localizer.localize("APP.GENERAL.SELL")
                        },
                    ),
                ),
                DydxFundingDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.PRICE"),
                    value = DydxFundingDetailsView.Item.ItemValue.Number(
                        price,
                    ),
                ),
                DydxFundingDetailsView.Item(
                    title = localizer.localize("APP.GENERAL.CREATED_AT"),
                    value = DydxFundingDetailsView.Item.ItemValue.StringValue(
                        createdAt,
                    ),
                ),
            ),
        )
    }
}
