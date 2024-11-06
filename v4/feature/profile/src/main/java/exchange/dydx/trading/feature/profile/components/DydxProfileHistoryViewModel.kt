package exchange.dydx.trading.feature.profile.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.MarketConfigsAndAsset
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.ProfileRoutes
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxProfileHistoryViewModel @Inject constructor(
    val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
) : ViewModel(), DydxViewModel {

    private val maxItemCount = 4

    val state: Flow<DydxProfileHistoryView.ViewState?> =
        combine(
            abacusStateManager.state.selectedSubaccountFills,
            abacusStateManager.state.selectedSubaccountFundings,
            abacusStateManager.state.transfers,
            abacusStateManager.state.configsAndAssetMap,
        ) { fills, fundings, transfers, configsAndAssetMap ->
            createViewState(
                fills = fills ?: emptyList(),
                fundings = fundings ?: emptyList(),
                transfers = transfers ?: emptyList(),
                configsAndAsset = configsAndAssetMap ?: emptyMap(),
            )
        }
            .distinctUntilChanged()

    private fun createViewState(
        fills: List<SubaccountFill>,
        fundings: List<SubaccountFundingPayment>,
        transfers: List<SubaccountTransfer>,
        configsAndAsset: Map<String, MarketConfigsAndAsset>,
    ): DydxProfileHistoryView.ViewState {
        var fills = fills.subList(0, maxItemCount.coerceAtMost(fills.size))
        var fundings = fundings.subList(0, maxItemCount.coerceAtMost(fundings.size))
        var transfers = transfers.subList(0, maxItemCount.coerceAtMost(transfers.size))

        val items: MutableList<DydxProfileHistoryItemView.ViewState> = mutableListOf()
        repeat(maxItemCount) {
            val item = mostRecentOf(
                fills.firstOrNull(),
                fundings.firstOrNull(),
                transfers.firstOrNull(),
            )
            if (item is SubaccountFill) {
                createFillItem(item, configsAndAsset)?.let { items.add(it) }
                fills = fills.subList(1, fills.size)
            } else if (item is SubaccountFundingPayment) {
                createFundingItem(item, configsAndAsset)?.let { items.add(it) }
                fundings = fundings.subList(1, fundings.size)
            } else if (item is SubaccountTransfer) {
                createTransferItem(item)?.let { items.add(it) }
                transfers = transfers.subList(1, transfers.size)
            }
        }

        return DydxProfileHistoryView.ViewState(
            localizer = localizer,
            items = items,
            tapAction = {
                router.navigateTo(
                    route = ProfileRoutes.history,
                    presentation = DydxRouter.Presentation.Push,
                )
            },
        )
    }

    private fun mostRecentOf(
        fill: SubaccountFill?,
        funding: SubaccountFundingPayment?,
        transfer: SubaccountTransfer?
    ): Any? {
        val fillTime = fill?.createdAtMilliseconds ?: 0.0
        val fundingTime = funding?.effectiveAtMilliSeconds ?: 0.0
        val transferTime = transfer?.updatedAtMilliseconds ?: 0.0

        return when {
            fillTime > fundingTime && fillTime > transferTime -> fill
            fundingTime > transferTime -> funding
            else -> transfer
        }
    }

    private fun createFillItem(
        fill: SubaccountFill,
        configsAndAsset: Map<String, MarketConfigsAndAsset>
    ): DydxProfileHistoryItemView.ViewState? {
        val configsAndAsset = configsAndAsset[fill.marketId] ?: return null
        val configs = configsAndAsset.configs ?: return null
        val asset = configsAndAsset.asset ?: return null

        val action = DydxProfileHistoryItemView.ViewState.ActionType.Fill(
            sideTextViewModel = SideTextView.ViewState(
                side = when (fill.side) {
                    OrderSide.Buy -> SideTextView.Side.Buy
                    OrderSide.Sell -> SideTextView.Side.Sell
                },
                localizer = localizer,
            ),
            string = asset.displayableAssetId,
        )
        val side = SideTextView.ViewState(
            side = when (fill.side) {
                OrderSide.Buy -> SideTextView.Side.Long
                OrderSide.Sell -> SideTextView.Side.Short
            },
            coloringOption = SideTextView.ColoringOption.NONE,
            localizer = localizer,
        )
        val type = DydxProfileHistoryItemView.ViewState.TypeUnion.String(
            string = localizer.localize(fill.resources.typeStringKey ?: "-"),
        )
        val size = formatter.localFormatted(fill.size, configs.displayStepSizeDecimals ?: 1)
        return DydxProfileHistoryItemView.ViewState(
            localizer = localizer,
            action = action,
            side = side,
            type = type,
            amount = size,
        )
    }

    private fun createFundingItem(
        funding: SubaccountFundingPayment,
        configsAndAsset: Map<String, MarketConfigsAndAsset>
    ): DydxProfileHistoryItemView.ViewState? {
        val configsAndAsset = configsAndAsset[funding.marketId] ?: return null
        val configs = configsAndAsset.configs ?: return null
        val asset = configsAndAsset.asset ?: return null

        val action = DydxProfileHistoryItemView.ViewState.ActionType.String(
            string = localizer.localize("APP.GENERAL.FUNDING_RATE_CHART_SHORT"),
        )
        val side = SideTextView.ViewState(
            side = if (funding.positionSize > 0) SideTextView.Side.Long else SideTextView.Side.Short,
            coloringOption = SideTextView.ColoringOption.NONE,
            localizer = localizer,
        )
        val type = DydxProfileHistoryItemView.ViewState.TypeUnion.Token(
            tokenTextViewModel = TokenTextView.ViewState(
                symbol = asset.displayableAssetId,
            ),
        )
        val size = formatter.dollar(funding.payment, 4)
        return DydxProfileHistoryItemView.ViewState(
            localizer = localizer,
            action = action,
            side = side,
            type = type,
            amount = size,
        )
    }

    private fun createTransferItem(
        transfer: SubaccountTransfer,
    ): DydxProfileHistoryItemView.ViewState? {
        val action = DydxProfileHistoryItemView.ViewState.ActionType.String(
            string = localizer.localize(transfer.resources.typeStringKey ?: "-"),
        )
        val side = SideTextView.ViewState(
            side = SideTextView.Side.Custom("-"),
            coloringOption = SideTextView.ColoringOption.NONE,
            localizer = localizer,
        )
        val type = DydxProfileHistoryItemView.ViewState.TypeUnion.Token(
            tokenTextViewModel = TokenTextView.ViewState(
                symbol = transfer.asset ?: "-",
            ),
        )
        val size = formatter.localFormatted(transfer.amount, 2)
        return DydxProfileHistoryItemView.ViewState(
            localizer = localizer,
            action = action,
            side = side,
            type = type,
            amount = size,
        )
    }
}
