package exchange.dydx.dydxstatemanager

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.Compliance
import exchange.dydx.abacus.output.ComplianceStatus
import exchange.dydx.abacus.output.Configs
import exchange.dydx.abacus.output.Documentation
import exchange.dydx.abacus.output.LaunchIncentive
import exchange.dydx.abacus.output.LaunchIncentivePoints
import exchange.dydx.abacus.output.MarketCandles
import exchange.dydx.abacus.output.MarketConfigs
import exchange.dydx.abacus.output.MarketHistoricalFunding
import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.output.MarketTrade
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.TransferStatus
import exchange.dydx.abacus.output.User
import exchange.dydx.abacus.output.account.Account
import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.account.SubaccountPendingPosition
import exchange.dydx.abacus.output.account.SubaccountPosition
import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInput
import exchange.dydx.abacus.output.input.ClosePositionInput
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.TriggerOrdersInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.state.manager.ApiState
import exchange.dydx.abacus.state.manager.SingletonAsyncAbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletState
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.utilities.utils.combineState
import exchange.dydx.utilities.utils.mapState
import exchange.dydx.utilities.utils.mapStateWithThrottle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class AbacusState(
    val walletState: StateFlow<DydxWalletState?>,
    private val perpetualState: StateFlow<PerpetualState?>,
    private val apiPerpetualState: StateFlow<ApiState?>,
    private val errorsPerpetualState: StateFlow<List<ParsingError>?>,
    val lastOrder: StateFlow<SubaccountOrder?>,
    val alerts: StateFlow<List<Notification>?>,
    val documentation: StateFlow<Documentation?>,
    private val abacusStateManager: SingletonAsyncAbacusStateManagerProtocol,
    private val parser: ParserProtocol,
    @CoroutineScopes.App private val appScope: CoroutineScope,
) {
    val isMainNet: Boolean
        get() = abacusStateManager.environment?.isMainNet ?: false

    /**
     Onboarded
     **/
    val onboarded: StateFlow<Boolean> by lazy {
        walletState
            .mapState(appScope) { walletState ->
                walletState?.currentWallet?.let { currentWallet ->
                    currentWallet.cosmoAddress?.isNotEmpty() == true
                } ?: false
            }
    }

    /**
     Current wallet
     **/
    val currentWallet: StateFlow<DydxWalletInstance?> by lazy {
        walletState
            .mapState(appScope) { it?.currentWallet }
    }

    val transfers: StateFlow<List<SubaccountTransfer>?> by lazy {
        perpetualState
            .mapState(appScope) {
                val subaccountNumber = subaccountNumber ?: return@mapState null
                it?.transfers?.get("$subaccountNumber")?.toList()
            }
    }

    /**
     TransferStatuses (Abacus state of transfer statuses)
     **/
    val transferStatuses: StateFlow<Map<String, TransferStatus>> by lazy {
        perpetualState
            .mapState(appScope) {
                it?.transferStatuses?.toMap() ?: emptyMap()
            }
    }

    /**
     Account
     **/
    val account: StateFlow<Account?> by lazy {
        perpetualState
            .mapState(appScope) { it?.account }
    }

    val hasAccount: StateFlow<Boolean> by lazy {
        account
            .mapState(appScope) { it != null }
    }

    /**
     Account balances (wallet balances)
     **/

    fun accountBalance(tokenDenom: String?): StateFlow<Double?> {
        return perpetualState
            .mapState(appScope) { state: PerpetualState? ->
                state?.account?.balances?.get(tokenDenom)?.amount?.toDoubleOrNull()
            }
    }

    fun stakingBalance(tokenDenom: String?): StateFlow<Double?> {
        return perpetualState
            .mapState(appScope) { state: PerpetualState? ->
                state?.account?.stakingBalances?.get(tokenDenom)?.amount?.toDoubleOrNull()
            }
    }

    /**
     Subaccount
     **/
    fun subaccount(subaccountNumber: Int): StateFlow<Subaccount?> {
        return perpetualState
            .mapState(appScope) { it?.subaccount(subaccountNumber) }
    }

    val selectedSubaccount: StateFlow<Subaccount?> by lazy {
        perpetualState
            .mapState(appScope) { state ->
                subaccountNumber?.let {
                    state?.subaccount(it)
                }
            }
    }

    val selectedSubaccountFills: StateFlow<List<SubaccountFill>?> by lazy {
        perpetualState
            .mapState(appScope) {
                if (subaccountNumber != null) {
                    it?.fills?.get("$subaccountNumber")?.toList()
                } else {
                    null
                }
            }
    }

    val selectedSubaccountFundings: StateFlow<List<SubaccountFundingPayment>?> by lazy {
        perpetualState
            .mapState(appScope) {
                if (subaccountNumber != null) {
                    it?.fundingPayments?.get("$subaccountNumber")?.toList()
                } else {
                    null
                }
            }
    }

    val selectedSubaccountPositions: StateFlow<List<SubaccountPosition>?> by lazy {
        selectedSubaccount
            .mapState(appScope) { subaccount ->
                subaccount?.openPositions
            }
    }

    val selectedSubaccountPendingPositions: StateFlow<List<SubaccountPendingPosition>?> by lazy {
        selectedSubaccount
            .mapState(appScope) { subaccount ->
                subaccount?.pendingPositions
            }
    }

    fun selectedSubaccountPositionOfMarket(marketId: String): StateFlow<SubaccountPosition?> {
        return selectedSubaccountPositions
            .mapState(appScope) { positions ->
                positions?.firstOrNull { position ->
                    position.id == marketId &&
                        (
                            position.side.current == PositionSide.SHORT ||
                                position.side.current == PositionSide.LONG
                            )
                }
            }
    }

    fun selectedSubaccountUnopenedPositionOfMarket(marketId: String): StateFlow<SubaccountPosition?> {
        return selectedSubaccountPositions
            .mapState(appScope) { positions ->
                positions?.firstOrNull { position ->
                    position.id == marketId && position.side.current == PositionSide.NONE
                }
            }
    }

    val selectedSubaccountOrders: StateFlow<List<SubaccountOrder>?> by lazy {
        selectedSubaccount
            .mapState(appScope) { subaccount ->
                subaccount?.orders
            }
    }

    fun selectedSubaccountOrdersOfMarket(marketId: String): StateFlow<List<SubaccountOrder>?> {
        return selectedSubaccountOrders
            .mapState(appScope) { orders ->
                orders?.filter { order ->
                    order.marketId == marketId
                }
            }
    }

    val selectedSubaccountPNLs: StateFlow<List<SubaccountHistoricalPNL>?> by lazy {
        perpetualState
            .mapState(appScope) {
                if (subaccountNumber != null) {
                    it?.historicalPnl?.get("$subaccountNumber")?.toList()
                } else {
                    null
                }
            }
    }

    /**
     Fundings
     **/
    val historicalFundingsMap: StateFlow<Map<String, List<MarketHistoricalFunding>>?> by lazy {
        perpetualState
            .mapState(appScope) {
                it?.historicalFundings?.toMap()
            }
    }

    /**
     Fundings of a given market
     **/
    fun historicalFundings(marketId: String): StateFlow<List<MarketHistoricalFunding>?> {
        return historicalFundingsMap
            .mapState(appScope) { it?.get(marketId) }
    }

    /**
     Market Summary
     **/
    val marketSummary: StateFlow<PerpetualMarketSummary?> by lazy {
        perpetualState
            .mapState(appScope) {
                it?.marketsSummary
            }
    }

    /**
     Map from market ID to Candles
     **/
    val candlesMap: StateFlow<Map<String, MarketCandles>?> by lazy {
        perpetualState
            .mapState(appScope) {
                it?.candles?.toMap()
            }
    }

    /**
     Candles of a given market
     **/
    fun candles(marketId: String): StateFlow<MarketCandles?> {
        return candlesMap
            .mapState(appScope) { it?.get(marketId) }
    }

    /**
     Map from market ID to Orderbook
     **/
    val orderbooksMap: StateFlow<Map<String, MarketOrderbook>?> by lazy {
        perpetualState
            .mapStateWithThrottle(appScope) {
                it?.orderbooks?.toMap()
            }
    }

    /**
     Orderbook of a given market
     **/
    fun orderbook(marketId: String): StateFlow<MarketOrderbook?> {
        return orderbooksMap
            .mapState(appScope) { it?.get(marketId) }
    }

    /**
     Map from market ID to Trades
     **/
    val tradesMap: StateFlow<Map<String, List<MarketTrade>>?> by lazy {
        perpetualState
            .mapState(appScope) {
                it?.trades?.toMap()
            }
    }

    /**
     Trades of a given market
     **/
    fun trade(marketId: String): StateFlow<List<MarketTrade>?> {
        return tradesMap
            .mapState(appScope) { it?.get(marketId) }
    }

    /**
     List of market IDs
     **/
    val marketIds: StateFlow<List<String>?> by lazy {
        marketSummary
            .mapState(appScope) { it?.marketIds() }
    }

    /**
     Map from market ID to Market
     **/
    val marketMap: StateFlow<Map<String, PerpetualMarket>?> by lazy {
        marketSummary
            .mapState(appScope) {
                it?.markets?.toMap()
            }
    }

    /**
     List Market ordrered by market IDs
     **/
    val marketList: StateFlow<List<PerpetualMarket>?> by lazy {
        combineState(
            marketIds,
            marketMap,
            appScope,
        ) { ids: List<String>?, map: Map<String, PerpetualMarket>? ->
            ids?.mapNotNull { id ->
                map?.get(id)
            }
        }
    }

    /**
     Market of a given market ID
     **/
    fun market(marketId: String): StateFlow<PerpetualMarket?> {
        return marketMap
            .mapState(appScope) {
                it?.get(marketId)
            }
    }

    /**
     Asset of given asset Id
     **/
    val assetMap: StateFlow<Map<String, Asset>?> by lazy {
        perpetualState
            .mapState(appScope) {
                it?.assets?.toMap()
            }
    }

    /**
     MarketConfigs and Asset map
     **/
    val configsAndAssetMap: StateFlow<Map<String, MarketConfigsAndAsset>?> by lazy {
        combineState(
            marketMap,
            assetMap,
            appScope,
        ) { marketMap: Map<String, PerpetualMarket>?, assetMap: Map<String, Asset>? ->
            val output = mutableMapOf<String, MarketConfigsAndAsset>()
            marketMap?.entries?.forEach { (marketId, market) ->
                output[marketId] =
                    MarketConfigsAndAsset(market.configs, assetMap?.get(market.assetId), market.assetId)
            }
            output
        }
    }

    /**
     Trade input
     **/
    val tradeInput: StateFlow<TradeInput?> by lazy {
        perpetualState
            .mapStateWithThrottle(appScope) { it?.input?.trade }
    }

    /**
     Close Position input
     **/
    val closePositionInput: StateFlow<ClosePositionInput?> by lazy {
        perpetualState
            .mapStateWithThrottle(appScope) { it?.input?.closePosition }
    }

    /**
     Transfer input
     **/
    val transferInput: StateFlow<TransferInput?> by lazy {
        perpetualState
            .mapStateWithThrottle(appScope) { it?.input?.transfer }
    }

    /**
     Input receipts
     **/
    val receipts: StateFlow<List<ReceiptLine>> by lazy {
        perpetualState
            .mapStateWithThrottle(appScope) { it?.input?.receiptLines ?: emptyList() }
    }

    /**
     Input validation
     **/
    val validationErrors: StateFlow<List<ValidationError>> by lazy {
        perpetualState
            .mapStateWithThrottle(appScope) { it?.input?.errors ?: emptyList() }
    }

    /**
     Backend Error
     */
    val backendError: StateFlow<ParsingError?> by lazy {
        errorsPerpetualState
            .mapState(appScope) { errors ->
                errors?.firstOrNull { error ->
                    when (error.type) {
                        ParsingErrorType.BackendError -> true
                        else -> false
                    }
                }
            }
    }

    /**
     Config
     */
    val configs: StateFlow<Configs?> by lazy {
        perpetualState
            .mapState(appScope) { it?.configs }
    }

    /**
     User
     **/
    val user: StateFlow<User?> by lazy {
        perpetualState
            .mapState(appScope) { it?.wallet?.user }
    }

    /**
     Launch Incentives
     **/
    val launchIncentive: StateFlow<LaunchIncentive?> by lazy {
        perpetualState
            .mapState(appScope) {
                it?.launchIncentive
            }
    }

    val launchIncentivePoints: StateFlow<LaunchIncentivePoints?> by lazy {
        perpetualState
            .mapState(appScope) {
                it?.account?.launchIncentivePoints
            }
    }

    /**
     ApiState
     */
    val apiState: StateFlow<ApiState?> = apiPerpetualState

    /**
     Restriction
     */
    val restriction: StateFlow<Restriction> by lazy {
        perpetualState
            .mapState(appScope) { it?.restriction?.restriction ?: Restriction.NO_RESTRICTION }
    }

    /**
     Compliance
     */
    val compliance: StateFlow<Compliance> by lazy {
        perpetualState
            .mapState(appScope) { it?.compliance ?: Compliance(null, ComplianceStatus.UNKNOWN, null, null) }
    }

    /**
     Trigger order input
     **/
    val triggerOrdersInput: StateFlow<TriggerOrdersInput?> by lazy {
        perpetualState
            .mapStateWithThrottle(appScope) {
                it?.input?.triggerOrders
            }
    }

    /**
     * Adjust Margin input
     */
    val adjustMarginInput: StateFlow<AdjustIsolatedMarginInput?> by lazy {
        perpetualState
            .mapStateWithThrottle(appScope) { it?.input?.adjustIsolatedMargin }
    }

    /**
     * Vault
     */
    val vault: StateFlow<exchange.dydx.abacus.output.Vault?> by lazy {
        perpetualState
            .mapState(appScope) { it?.vault }
    }

    val subaccountNumber: Int?
        get() = parser.asInt(abacusStateManager.subaccountNumber)
}

data class MarketConfigsAndAsset(
    val configs: MarketConfigs?,
    val asset: Asset?,
    val assetId: String?,
)
