package exchange.dydx.trading.feature.portfolio.components.fundings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.feature.shared.views.IntervalText
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.math.sign

@HiltViewModel
class DydxPortfolioFundingsViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxPortfolioFundingsView.ViewState?> = combine(
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
    ): DydxPortfolioFundingsView.ViewState {
        val fundings = if (marketId != null) {
            fundings?.filter { it.marketId == marketId }
        } else {
            fundings
        }
        return DydxPortfolioFundingsView.ViewState(
            localizer = localizer,
            fundings = fundings?.mapNotNull { funding ->
                val market = marketMap?.get(funding.marketId) ?: return@mapNotNull null
                val asset = assetMap?.get(market.assetId) ?: return@mapNotNull null
                val longValue = funding.createdAtMilliseconds.toLong()

                val amount = formatter.dollar(funding.payment.absoluteValue, digits = 4)
                val rate = formatter.percent(funding.rate, digits = 6)
                val sign: PlatformUISign
                val status: DydxPortfolioFundingItemView.FundingStatus
                if (funding.payment >= 0.0) {
                    sign = PlatformUISign.Plus
                    status = DydxPortfolioFundingItemView.FundingStatus.earned
                } else {
                    sign = PlatformUISign.Minus
                    status = DydxPortfolioFundingItemView.FundingStatus.paid
                }

                val stepSize = market.configs?.displayStepSizeDecimals ?: 1
                val positionSize = formatter.raw(funding.positionSize.absoluteValue, digits = stepSize)

                DydxPortfolioFundingItemView.ViewState(
                    localizer = localizer,
                    id = funding.id,
                    date = IntervalText.ViewState(
                        date = Instant.ofEpochMilli(longValue),
                    ),
                    logoUrl = asset.resources?.imageUrl,
                    status = status,
                    amount = SignedAmountView.ViewState(
                        text = amount,
                        sign = sign,
                        coloringOption = SignedAmountView.ColoringOption.SignOnly,
                    ),
                    rate = SignedAmountView.ViewState(
                        text = rate,
                        sign = if (funding.rate >= 0.0) {
                            PlatformUISign.Plus
                        } else {
                            PlatformUISign.Minus
                        },
                        coloringOption = SignedAmountView.ColoringOption.AllText,
                    ),
                    sideText = SideTextView.ViewState(
                        localizer = localizer,
                        side = if (funding.positionSize >= 0.0) {
                            SideTextView.Side.Buy
                        } else {
                            SideTextView.Side.Sell
                        },
                    ),
                    position = positionSize,
                    token = TokenTextView.ViewState(
                        symbol = asset.displayableAssetId,
                    ),
                )
            } ?: listOf(),
            onTapAction = { id ->
                router.navigateTo(
                    route = PortfolioRoutes.funding_details + "/$id",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
        )
    }
}
