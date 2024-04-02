package exchange.dydx.dydxstatemanager

import com.hoc081098.flowext.throttleTime
import exchange.dydx.abacus.output.Account
import exchange.dydx.abacus.output.Asset
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
import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.SubaccountFill
import exchange.dydx.abacus.output.SubaccountFundingPayment
import exchange.dydx.abacus.output.SubaccountHistoricalPNL
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.SubaccountPosition
import exchange.dydx.abacus.output.SubaccountTransfer
import exchange.dydx.abacus.output.TransferStatus
import exchange.dydx.abacus.output.User
import exchange.dydx.abacus.output.input.ClosePositionInput
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.state.manager.ApiState
import exchange.dydx.abacus.state.manager.SingletonAsyncAbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferInstance
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferState
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.newSingleThreadContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class AbacusState @Inject constructor(
    private val walletStatePublisher: Flow<DydxWalletState?>,
    private val perpetualStatePublisher: Flow<PerpetualState?>,
    private val apiStatePublisher: Flow<ApiState?>,
    private val errorsStatePublisher: Flow<List<ParsingError>?>,
    private val lastOrderPublisher: Flow<SubaccountOrder?>,
    private val alertsPublisher: Flow<List<Notification>?>,
    private val documentationPublisher: Flow<Documentation?>,
    private val transferStatePublisher: Flow<DydxTransferState?>,
    private val abacusStateManager: SingletonAsyncAbacusStateManagerProtocol,
    private var parser: ParserProtocol,
) {
    var isMainNet: Boolean? = null
        get() = abacusStateManager.environment?.isMainNet ?: false

    /**
     Onboarded
     **/
    val onboarded: Flow<Boolean> by lazy {
        walletStatePublisher
            .map { walletState ->
                walletState?.currentWallet?.let { currentWallet ->
                    currentWallet.cosmoAddress?.isNotEmpty() == true
                } ?: false
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     WalletState
     **/
    val walletState: Flow<DydxWalletState?> by lazy {
        walletStatePublisher
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Current wallet
     **/
    val currentWallet: Flow<DydxWalletInstance?> by lazy {
        walletState
            .map { it?.currentWallet }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Frontend alerts
     **/
    val alerts: Flow<List<Notification>?> by lazy {
        alertsPublisher
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Documentation
     **/
    val documentation: Flow<Documentation?> by lazy {
        documentationPublisher
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     TransferState (client state of the pending transfers)
     **/
    val transferState: Flow<DydxTransferState?> by lazy {
        transferStatePublisher
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    val transfers: Flow<List<SubaccountTransfer>?> by lazy {
        statePublisher
            .map { it?.transfers }
            .map {
                val subaccountNumber = subaccountNumber ?: return@map null
                it?.get(subaccountNumber)?.toList()
            }
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    fun transferInstance(transactionHash: String?): Flow<DydxTransferInstance?> {
        return transferState
            .map { it?.transfers?.first { it.transactionHash == transactionHash } }
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     TransferStatuses (Abacus state of transfer statuses)
     **/
    val transferStatuses: Flow<Map<String, TransferStatus>?> by lazy {
        perpetualStatePublisher
            .map { it?.transferStatuses }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Account
     **/
    val account: Flow<Account?> by lazy {
        perpetualStatePublisher
            .map { it?.account }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    val hasAccount: Flow<Boolean> by lazy {
        account
            .map { it != null }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Account balances (wallet balances)
     **/

    fun accountBalance(tokenDenom: String?): Flow<Double?> {
        return statePublisher
            .map { state: PerpetualState? ->
                state?.account?.balances?.get(tokenDenom)?.amount?.toDoubleOrNull()
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    fun stakingBalance(tokenDenom: String?): Flow<Double?> {
        return statePublisher
            .map { state: PerpetualState? ->
                state?.account?.stakingBalances?.get(tokenDenom)?.amount?.toDoubleOrNull()
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Subaccount
     **/
    fun subaccount(subaccountNumber: String): Flow<Subaccount?> {
        return account
            .map { it?.subaccounts?.get(subaccountNumber) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    val selectedSubaccount: Flow<Subaccount?> by lazy {
        statePublisher
            .map {
                if (subaccountNumber != null) {
                    it?.account?.subaccounts?.get(subaccountNumber)
                } else {
                    null
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    val selectedSubaccountFills: Flow<List<SubaccountFill>?> by lazy {
        statePublisher
            .map {
                if (subaccountNumber != null) {
                    it?.fills?.get(subaccountNumber)?.toList()
                } else {
                    null
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    val selectedSubaccountFundings: Flow<List<SubaccountFundingPayment>?> by lazy {
        statePublisher
            .map {
                if (subaccountNumber != null) {
                    it?.fundingPayments?.get(subaccountNumber)?.toList()
                } else {
                    null
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    val selectedSubaccountPositions: Flow<List<SubaccountPosition>?> by lazy {
        selectedSubaccount
            .map { subaccount ->
                subaccount?.openPositions
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    val selectedSubaccountOrders: Flow<List<SubaccountOrder>?> by lazy {
        selectedSubaccount
            .map { subaccount ->
                subaccount?.orders
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    val selectedSubaccountPNLs: Flow<List<SubaccountHistoricalPNL>?> by lazy {
        statePublisher
            .map {
                if (subaccountNumber != null) {
                    it?.historicalPnl?.get(subaccountNumber)?.toList()
                } else {
                    null
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Fundings
     **/
    val historicalFundingsMap: Flow<Map<String, List<MarketHistoricalFunding>>?> by lazy {
        statePublisher
            .map { it?.historicalFundings }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Fundings of a given market
     **/
    fun historicalFundings(marketId: String): Flow<List<MarketHistoricalFunding>?> {
        return historicalFundingsMap
            .map { it?.get(marketId) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Market Summary
     **/
    val marketSummary: Flow<PerpetualMarketSummary?> by lazy {
        statePublisher
            .map {
                it?.marketsSummary
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Map from market ID to Candles
     **/
    val candlesMap: Flow<Map<String, MarketCandles>?> by lazy {
        statePublisher
            .map { it?.candles }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Candles of a given market
     **/
    fun candles(marketId: String): Flow<MarketCandles?> {
        return candlesMap
            .map { it?.get(marketId) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Map from market ID to Orderbook
     **/
    val orderbooksMap: Flow<Map<String, MarketOrderbook>?> by lazy {
        statePublisher
            .map {
                it?.orderbooks
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Orderbook of a given market
     **/
    fun orderbook(marketId: String): Flow<MarketOrderbook?> {
        return orderbooksMap
            .map { it?.get(marketId) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Map from market ID to Trades
     **/
    val tradesMap: Flow<Map<String, List<MarketTrade>>?> by lazy {
        statePublisher
            .map { it?.trades }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Trades of a given market
     **/
    fun trade(marketId: String): Flow<List<MarketTrade>?> {
        return tradesMap
            .map { it?.get(marketId) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     List of market IDs
     **/
    val markeeIds: Flow<List<String>?> by lazy {
        marketSummary
            .map { it?.marketIds() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Map from market ID to Market
     **/
    val marketMap: Flow<Map<String, PerpetualMarket>?> by lazy {
        marketSummary
            .map {
                it?.markets
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     List Market ordrered by market IDs
     **/
    val marketList: Flow<List<PerpetualMarket>?> by lazy {
        combine(
            markeeIds,
            marketMap,
        ) { ids: List<String>?, map: Map<String, PerpetualMarket>? ->
            ids?.mapNotNull { id ->
                map?.get(id)
            }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Market of a given market ID
     **/
    fun market(marketId: String): Flow<PerpetualMarket?> {
        return marketMap
            .map {
                it?.get(marketId)
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Asset of given asset Id
     **/
    val assetMap: Flow<Map<String, Asset>?> by lazy {
        statePublisher
            .map { it?.assets }
            .distinctUntilChanged()
            //   .throttleTime(1000)
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     MarketConfigs and Asset map
     **/
    val configsAndAssetMap: Flow<Map<String, MarketConfigsAndAsset>?> by lazy {
        combine(
            marketMap,
            statePublisher.map { it?.assets },
        ) { marketMap: Map<String, PerpetualMarket>?, assetMap: Map<String, Asset>? ->
            val output = mutableMapOf<String, MarketConfigsAndAsset>()
            marketMap?.forEach { (marketId, market) ->
                output[marketId] =
                    MarketConfigsAndAsset(market.configs, assetMap?.get(market.assetId), market.assetId)
            }
            output
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Trade input
     **/
    val tradeInput: Flow<TradeInput?> by lazy {
        statePublisher
            .map { it?.input?.trade }
            .distinctUntilChanged()
            .throttleTime(10)
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Close Position input
     **/
    val closePositionInput: Flow<ClosePositionInput?> by lazy {
        statePublisher
            .map { it?.input?.closePosition }
            .distinctUntilChanged()
            .throttleTime(10)
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Transfer input
     **/
    val transferInput: Flow<TransferInput?> by lazy {
        statePublisher
            .map { it?.input?.transfer }
            .distinctUntilChanged()
            .throttleTime(10)
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Input receipts
     **/
    val receipts: Flow<List<ReceiptLine>> by lazy {
        statePublisher
            .map { it?.input?.receiptLines ?: emptyList() }
            .distinctUntilChanged()
            .throttleTime(10)
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Input validation
     **/
    val validationErrors: Flow<List<ValidationError>> by lazy {
        statePublisher
            .map { it?.input?.errors ?: emptyList() }
            .distinctUntilChanged()
            .throttleTime(10)
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Last Order
     */
    val lastOrder: Flow<SubaccountOrder?> by lazy {
        lastOrderPublisher
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Backend Error
     */
    val backendError: Flow<ParsingError?> by lazy {
        errorsStatePublisher
            .map { errors ->
                errors?.firstOrNull { error ->
                    when (error.type) {
                        ParsingErrorType.BackendError -> true
                        else -> false
                    }
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Config
     */
    val configs: Flow<exchange.dydx.abacus.output.Configs?> by lazy {
        statePublisher
            .map { it?.configs }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     User
     **/
    val user: Flow<User?> by lazy {
        statePublisher
            .map { it?.wallet?.user }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Launch Incentives
     **/
    val launchIncentive: Flow<LaunchIncentive?> by lazy {
        statePublisher
            .map {
                it?.launchIncentive
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    val launchIncentivePoints: Flow<LaunchIncentivePoints?> by lazy {
        statePublisher
            .map {
                it?.account?.launchIncentivePoints
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     ApiState
     */
    val apiState: Flow<ApiState?> by lazy {
        apiStatePublisher
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    /**
     Restriction
     */
    val restriction: Flow<Restriction> by lazy {
        statePublisher
            .map { it?.restriction?.restriction ?: Restriction.NO_RESTRICTION }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    private val statePublisher: Flow<PerpetualState?> by lazy {
        perpetualStatePublisher
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(stateManagerScope, SharingStarted.Lazily, 1)
    }

    private val stateManagerScope = CoroutineScope(newSingleThreadContext("AbacusStateManager"))

    private val subaccountNumber: String?
        get() = parser.asString(abacusStateManager.subaccountNumber)
}

data class MarketConfigsAndAsset(
    val configs: MarketConfigs?,
    val asset: Asset?,
    val assetId: String?,
)
