package exchange.dydx.dydxstatemanager

import com.hoc081098.flowext.ThrottleConfiguration
import com.hoc081098.flowext.throttleTime
import exchange.dydx.abacus.output.Account
import exchange.dydx.abacus.output.Asset
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
import exchange.dydx.abacus.output.input.TriggerOrdersInput
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class AbacusState(
    val walletState: StateFlow<DydxWalletState?>,
    private val perpetualState: StateFlow<PerpetualState?>,
    private val apiPerpetualState: StateFlow<ApiState?>,
    private val errorsPerpetualState: StateFlow<List<ParsingError>?>,
    private val lastOrderPublisher: StateFlow<SubaccountOrder?>,
    val alerts: StateFlow<List<Notification>?>,
    val documentation: StateFlow<Documentation?>,
    val transferState: StateFlow<DydxTransferState?>,
    private val abacusStateManager: SingletonAsyncAbacusStateManagerProtocol,
    private val parser: ParserProtocol,
) {

    private val stateManagerScope = MainScope()

    var isMainNet: Boolean? = null
        get() = abacusStateManager.environment?.isMainNet ?: false

    /**
     Onboarded
     **/
    val onboarded: StateFlow<Boolean> by lazy {
        walletState
            .map { walletState ->
                walletState?.currentWallet?.let { currentWallet ->
                    currentWallet.cosmoAddress?.isNotEmpty() == true
                } ?: false
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), false)
    }

    /**
     Current wallet
     **/
    val currentWallet: StateFlow<DydxWalletInstance?> by lazy {
        walletState
            .map { it?.currentWallet }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    val transfers: StateFlow<List<SubaccountTransfer>?> by lazy {
        perpetualState
            .map { it?.transfers }
            .map {
                val subaccountNumber = subaccountNumber ?: return@map null
                it?.get(subaccountNumber)?.toList()
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    fun transferInstance(transactionHash: String?): StateFlow<DydxTransferInstance?> {
        return transferState
            .map { it?.transfers?.first { it.transactionHash == transactionHash } }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     TransferStatuses (Abacus state of transfer statuses)
     **/
    val transferStatuses: StateFlow<Map<String, TransferStatus>> by lazy {
        perpetualState
            .map { it?.transferStatuses ?: emptyMap() }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), emptyMap())
    }

    /**
     Account
     **/
    val account: StateFlow<Account?> by lazy {
        perpetualState
            .map { it?.account }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    val hasAccount: StateFlow<Boolean> by lazy {
        account
            .map { it != null }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), false)
    }

    /**
     Account balances (wallet balances)
     **/

    fun accountBalance(tokenDenom: String?): StateFlow<Double?> {
        return perpetualState
            .map { state: PerpetualState? ->
                state?.account?.balances?.get(tokenDenom)?.amount?.toDoubleOrNull()
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    fun stakingBalance(tokenDenom: String?): StateFlow<Double?> {
        return perpetualState
            .map { state: PerpetualState? ->
                state?.account?.stakingBalances?.get(tokenDenom)?.amount?.toDoubleOrNull()
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Subaccount
     **/
    fun subaccount(subaccountNumber: String): StateFlow<Subaccount?> {
        return account
            .map { it?.subaccounts?.get(subaccountNumber) }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    val selectedSubaccount: StateFlow<Subaccount?> by lazy {
        perpetualState
            .map {
                if (subaccountNumber != null) {
                    it?.account?.subaccounts?.get(subaccountNumber)
                } else {
                    null
                }
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    val selectedSubaccountFills: StateFlow<List<SubaccountFill>?> by lazy {
        perpetualState
            .map {
                if (subaccountNumber != null) {
                    it?.fills?.get(subaccountNumber)?.toList()
                } else {
                    null
                }
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    val selectedSubaccountFundings: StateFlow<List<SubaccountFundingPayment>?> by lazy {
        perpetualState
            .map {
                if (subaccountNumber != null) {
                    it?.fundingPayments?.get(subaccountNumber)?.toList()
                } else {
                    null
                }
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    val selectedSubaccountPositions: StateFlow<List<SubaccountPosition>?> by lazy {
        selectedSubaccount
            .map { subaccount ->
                subaccount?.openPositions
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    val selectedSubaccountOrders: StateFlow<List<SubaccountOrder>?> by lazy {
        selectedSubaccount
            .map { subaccount ->
                subaccount?.orders
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    val selectedSubaccountPNLs: StateFlow<List<SubaccountHistoricalPNL>?> by lazy {
        perpetualState
            .map {
                if (subaccountNumber != null) {
                    it?.historicalPnl?.get(subaccountNumber)?.toList()
                } else {
                    null
                }
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Fundings
     **/
    val historicalFundingsMap: StateFlow<Map<String, List<MarketHistoricalFunding>>?> by lazy {
        perpetualState
            .map { it?.historicalFundings }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Fundings of a given market
     **/
    fun historicalFundings(marketId: String): StateFlow<List<MarketHistoricalFunding>?> {
        return historicalFundingsMap
            .map { it?.get(marketId) }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Market Summary
     **/
    val marketSummary: StateFlow<PerpetualMarketSummary?> by lazy {
        perpetualState
            .map {
                it?.marketsSummary
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Map from market ID to Candles
     **/
    val candlesMap: StateFlow<Map<String, MarketCandles>?> by lazy {
        perpetualState
            .map { it?.candles }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Candles of a given market
     **/
    fun candles(marketId: String): StateFlow<MarketCandles?> {
        return candlesMap
            .map { it?.get(marketId) }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Map from market ID to Orderbook
     **/
    val orderbooksMap: StateFlow<Map<String, MarketOrderbook>?> by lazy {
        perpetualState
            .map {
                it?.orderbooks
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Orderbook of a given market
     **/
    fun orderbook(marketId: String): StateFlow<MarketOrderbook?> {
        return orderbooksMap
            .map { it?.get(marketId) }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Map from market ID to Trades
     **/
    val tradesMap: StateFlow<Map<String, List<MarketTrade>>?> by lazy {
        perpetualState
            .map { it?.trades }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Trades of a given market
     **/
    fun trade(marketId: String): StateFlow<List<MarketTrade>?> {
        return tradesMap
            .map { it?.get(marketId) }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     List of market IDs
     **/
    val markeeIds: StateFlow<List<String>?> by lazy {
        marketSummary
            .map { it?.marketIds() }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Map from market ID to Market
     **/
    val marketMap: StateFlow<Map<String, PerpetualMarket>?> by lazy {
        marketSummary
            .map {
                it?.markets
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     List Market ordrered by market IDs
     **/
    val marketList: StateFlow<List<PerpetualMarket>?> by lazy {
        combine(
            markeeIds,
            marketMap,
        ) { ids: List<String>?, map: Map<String, PerpetualMarket>? ->
            ids?.mapNotNull { id ->
                map?.get(id)
            }
        }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Market of a given market ID
     **/
    fun market(marketId: String): StateFlow<PerpetualMarket?> {
        return marketMap
            .map {
                it?.get(marketId)
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Asset of given asset Id
     **/
    val assetMap: StateFlow<Map<String, Asset>?> by lazy {
        perpetualState
            .map { it?.assets }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     MarketConfigs and Asset map
     **/
    val configsAndAssetMap: StateFlow<Map<String, MarketConfigsAndAsset>?> by lazy {
        combine(
            marketMap,
            perpetualState.map { it?.assets },
        ) { marketMap: Map<String, PerpetualMarket>?, assetMap: Map<String, Asset>? ->
            val output = mutableMapOf<String, MarketConfigsAndAsset>()
            marketMap?.forEach { (marketId, market) ->
                output[marketId] =
                    MarketConfigsAndAsset(market.configs, assetMap?.get(market.assetId), market.assetId)
            }
            output
        }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Trade input
     **/
    val tradeInput: StateFlow<TradeInput?> by lazy {
        perpetualState
            .map { it?.input?.trade }
            .throttleTime(10, throttleConfiguration = ThrottleConfiguration.LEADING_AND_TRAILING)
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Close Position input
     **/
    val closePositionInput: StateFlow<ClosePositionInput?> by lazy {
        perpetualState
            .map { it?.input?.closePosition }
            .throttleTime(10, throttleConfiguration = ThrottleConfiguration.LEADING_AND_TRAILING)
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Transfer input
     **/
    val transferInput: StateFlow<TransferInput?> by lazy {
        perpetualState
            .map { it?.input?.transfer }
            .throttleTime(10, throttleConfiguration = ThrottleConfiguration.LEADING_AND_TRAILING)
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Input receipts
     **/
    val receipts: StateFlow<List<ReceiptLine>> by lazy {
        perpetualState
            .map { it?.input?.receiptLines ?: emptyList() }
            .throttleTime(10, throttleConfiguration = ThrottleConfiguration.LEADING_AND_TRAILING)
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), emptyList())
    }

    /**
     Input validation
     **/
    val validationErrors: StateFlow<List<ValidationError>> by lazy {
        perpetualState
            .map { it?.input?.errors ?: emptyList() }
            .throttleTime(10, throttleConfiguration = ThrottleConfiguration.LEADING_AND_TRAILING)
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), emptyList())
    }

    /**
     Last Order
     */
    val lastOrder: StateFlow<SubaccountOrder?> by lazy {
        lastOrderPublisher
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Backend Error
     */
    val backendError: StateFlow<ParsingError?> by lazy {
        errorsPerpetualState
            .map { errors ->
                errors?.firstOrNull { error ->
                    when (error.type) {
                        ParsingErrorType.BackendError -> true
                        else -> false
                    }
                }
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Config
     */
    val configs: StateFlow<Configs?> by lazy {
        perpetualState
            .map { it?.configs }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     User
     **/
    val user: StateFlow<User?> by lazy {
        perpetualState
            .map { it?.wallet?.user }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    /**
     Launch Incentives
     **/
    val launchIncentive: StateFlow<LaunchIncentive?> by lazy {
        perpetualState
            .map {
                it?.launchIncentive
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
    }

    val launchIncentivePoints: StateFlow<LaunchIncentivePoints?> by lazy {
        perpetualState
            .map {
                it?.account?.launchIncentivePoints
            }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), null)
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
            .map { it?.restriction?.restriction ?: Restriction.NO_RESTRICTION }
            .stateIn(stateManagerScope, SharingStarted.WhileSubscribed(), Restriction.NO_RESTRICTION)
    }

    /**
     Trigger order input
     **/
    val triggerOrdersInput: StateFlow<TriggerOrdersInput?> by lazy {
        perpetualState
            .map {
                it?.input?.triggerOrders
            }
            .throttleTime(10, throttleConfiguration = ThrottleConfiguration.LEADING_AND_TRAILING)
            .stateIn(stateManagerScope, SharingStarted.Lazily, null)
    }
    private val subaccountNumber: String?
        get() = parser.asString(abacusStateManager.subaccountNumber)
}

data class MarketConfigsAndAsset(
    val configs: MarketConfigs?,
    val asset: Asset?,
    val assetId: String?,
)
