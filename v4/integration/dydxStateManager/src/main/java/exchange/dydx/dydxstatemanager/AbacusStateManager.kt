package exchange.dydx.dydxstatemanager

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import exchange.dydx.abacus.output.Documentation
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.PresentationProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.ApiState
import exchange.dydx.abacus.state.manager.GasToken
import exchange.dydx.abacus.state.manager.HistoricalPnlPeriod
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.state.manager.SingletonAsyncAbacusStateManagerProtocol
import exchange.dydx.abacus.state.manager.TokenInfo
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.v2.manager.AsyncAbacusStateManagerV2
import exchange.dydx.abacus.state.v2.supervisor.AppConfigsV2
import exchange.dydx.abacus.state.v2.supervisor.NotificationProviderType
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletStateManagerProtocol
import exchange.dydx.dydxstatemanager.protocolImplementations.LanguageKey
import exchange.dydx.dydxstatemanager.protocolImplementations.UIImplementationsExtensions
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.R
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.featureflags.DydxFeatureFlag
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientProtocol
import exchange.dydx.trading.integration.statsig.StatsigFlags
import exchange.dydx.trading.integration.statsig.StatsigInitWorker
import exchange.dydx.utilities.utils.DebugEnabled
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

interface AbacusStateManagerProtocol {

    sealed class SubmissionStatus {
        object Success : SubmissionStatus()
        data class Failed(val error: ParsingError?) : SubmissionStatus()
    }

    val deploymentUri: String
    val state: AbacusState
    val availableEnvironments: List<SelectionOption>
    val currentEnvironmentId: StateFlow<String?>
    val environment: V4Environment?
    val documentation: StateFlow<Documentation?>
    val marketId: StateFlow<String?>
    val candlesPeriod: StateFlow<String>

    fun setEnvironmentId(environment: String?)

    fun setV4(ethereumAddress: String?, walletId: String?, cosmosAddress: String, mnemonic: String)

    fun logOut()
    fun replaceCurrentWallet()
    fun setMarket(marketId: String?)
    fun setCandlesPeriod(candlesPeriod: String)
    fun setHistoricalPNLPeriod(period: HistoricalPnlPeriod)
    fun setHistoricalTradingRewardsPeriod(period: HistoricalTradingRewardsPeriod)

    fun startTrade()
    fun startClosePosition(marketId: String)
    fun startDeposit()
    fun startWithdrawal()
    fun startTransferOut()
    fun trade(input: String?, type: TradeInputField?)
    fun closePosition(input: String?, type: ClosePositionInputField)
    fun transfer(input: String?, type: TransferInputField?)
    fun faucet(amount: Int, statusCallback: (SubmissionStatus) -> Unit)
    fun setOrderbookMultiplier(multiplier: OrderbookGrouping)

    fun placeOrder(statusCallback: (SubmissionStatus) -> Unit)
    fun closePosition(statusCallback: (SubmissionStatus) -> Unit)
    fun cancelOrder(orderId: String, statusCallback: (SubmissionStatus) -> Unit)

    fun transferStatus(
        hash: String,
        fromChainId: String?,
        toChainId: String?,
        isCctp: Boolean,
        requestId: String?
    )

    fun screen(address: String, callback: ((Restriction) -> Unit))
    fun commitCCTPWithdraw(callback: (Boolean, ParsingError?, Any?) -> Unit)

    fun triggerOrders(input: String?, type: TriggerOrdersInputField?)
    fun commitTriggerOrders(callback: (SubmissionStatus) -> Unit)

    fun adjustIsolatedMargin(data: String?, type: AdjustIsolatedMarginInputField?)
    fun commitAdjustIsolatedMargin(statusCallback: (SubmissionStatus) -> Unit)

    fun setGasToken(gasToken: GasToken)

    // extensions
    fun resetTransferInputFields() {
        transfer(null, TransferInputField.size)
        transfer(null, TransferInputField.usdcSize)
        transfer(null, TransferInputField.type)
    }

    fun resetTriggerOrders() {
        val fields = TriggerOrdersInputField.values().filter {
            it != TriggerOrdersInputField.marketId
        }
        for (field in fields) {
            triggerOrders(null, field)
        }
    }

    fun registerPushToken(token: String, language: String)
    fun refreshVaultAccount()
}

// Temporary location, should probably make a separate dagger-qualifiers module.
@Qualifier
annotation class EnvKey

@Singleton
class AbacusStateManager @Inject constructor(
    private val application: Application,
    private val appConfig: AppConfig,
    private val ioImplementations: IOImplementations,
    private val walletStateManager: DydxWalletStateManagerProtocol,
    private val transferStateManager: DydxTransferStateManagerProtocol,
    private val cosmosClient: CosmosV4ClientProtocol,
    private val preferencesStore: SharedPreferencesStore,
    @LanguageKey private val preferenceKey: String,
    @EnvKey private val envKey: String,
    private val featureFlags: DydxFeatureFlags,
    @CoroutineScopes.App private val appScope: CoroutineScope,
    parser: ParserProtocol,
    private val presentationProtocol: PresentationProtocol,
    private val statsigFlags: StatsigFlags,
    private val statsigInitWorker: StatsigInitWorker,
) : AbacusStateManagerProtocol, StateNotificationProtocol {

    private val perpetualStatePublisher: MutableStateFlow<PerpetualState?> = MutableStateFlow(null)
    private val apiStatePublisher: MutableStateFlow<ApiState?> = MutableStateFlow(null)
    private val errorsStatePublisher: MutableStateFlow<List<ParsingError>?> = MutableStateFlow(null)
    private val lastOrderPublisher: MutableStateFlow<SubaccountOrder?> = MutableStateFlow(null)
    private val alertsPublisher: MutableStateFlow<List<Notification>?> = MutableStateFlow(null)
    private val documentationPublisher: MutableStateFlow<Documentation?> = MutableStateFlow(null)

    private val asyncStateManager: SingletonAsyncAbacusStateManagerProtocol by lazy {
        statsigInitWorker.start() // Need to ensure statsig is initialized before we access flags.

        val language = preferencesStore.read(key = preferenceKey, defaultValue = "en")
        UIImplementationsExtensions.reset(language = language, ioImplementations)

        val deployment: String
        val appConfigsV2 = AppConfigsV2.forAppWithIsolatedMargins
        if (featureFlags.isFeatureEnabled(DydxFeatureFlag.force_mainnet)) {
            deployment = "MAINNET"
        } else {
            val appDeployment = application.getString(R.string.app_deployment)
            deployment = if (appDeployment == "MAINNET" && DebugEnabled.enabled(preferencesStore)) {
                // Force to TESTFLIGHT if user has enabled debug mode, so that both MAINNET and TESTNET can be
                // switched from Settings
                "TESTFLIGHT"
            } else {
                appDeployment
            }
        }

        // Disable Abacus logging since it's too verbose.  Enable it if you need to debug Abacus.
        if (BuildConfig.DEBUG) {
            appConfigsV2.enableLogger = false
        }

        appConfigsV2.staticTyping = featureFlags.isFeatureEnabled(DydxFeatureFlag.abacus_static_typing, default = true)
        appConfigsV2.onboardingConfigs.alchemyApiKey = application.getString(R.string.alchemy_api_key)
        appConfigsV2.metadataService = featureFlags.isFeatureEnabled(DydxFeatureFlag.metadata_service, default = true)
        appConfigsV2.accountConfigs.subaccountConfigs.notifications =
            listOf(
                NotificationProviderType.BlockReward,
                NotificationProviderType.Positions,
            )

        AsyncAbacusStateManagerV2(
            deploymentUri = deploymentUri,
            deployment = deployment,
            appConfigs = appConfigsV2,
            ioImplementations = ioImplementations,
            uiImplementations = UIImplementationsExtensions.shared!!,
            stateNotification = this,
            dataNotification = null,
            presentationProtocol = presentationProtocol,
        ).also {
            it.subaccountNumber = 0
        }
    }

    // MARK: AbacusStateManagerProtocol

    override val state: AbacusState = AbacusState(
        walletState = walletStateManager.state,
        perpetualState = perpetualStatePublisher,
        apiPerpetualState = apiStatePublisher,
        errorsPerpetualState = errorsStatePublisher,
        lastOrder = lastOrderPublisher,
        alerts = alertsPublisher,
        documentation = documentationPublisher,
        abacusStateManager = asyncStateManager,
        parser = parser,
        appScope = appScope,
    )

    override val deploymentUri: String
        get() {
            val urlOverride = featureFlags.valueForFeature(DydxFeatureFlag.deployment_url)
            return if (!urlOverride.isNullOrEmpty()) {
                urlOverride
            } else {
                "https://" + appConfig.appWebHost
            }
        }

    override val availableEnvironments: List<SelectionOption>
        get() = asyncStateManager.availableEnvironments

    override val currentEnvironmentId: MutableStateFlow<String?> = MutableStateFlow(null)

    override val environment: V4Environment?
        get() = asyncStateManager.environment

    override val documentation = MutableStateFlow<Documentation?>(null)

    override val marketId = MutableStateFlow<String?>(null)
    override val candlesPeriod = MutableStateFlow<String>("1DAY")

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.monitorConnectivity(application)
        }
    }

    override fun setEnvironmentId(environment: String?) {
        if (currentEnvironmentId.value != environment) {
            currentEnvironmentId.value = environment
            if (environment != null) {
                preferencesStore.save(environment, envKey)
            }
            if (asyncStateManager.environmentId != environment) {
                asyncStateManager.readyToConnect = false
                asyncStateManager.environmentId = environment
            }
            start()
        }
    }

    override fun setV4(
        ethereumAddress: String?,
        walletId: String?,
        cosmosAddress: String,
        mnemonic: String
    ) {
        cosmosClient.connectWallet(mnemonic) {
            val wallet = DydxWalletInstance.v4(ethereumAddress, walletId, cosmosAddress, mnemonic)
            walletStateManager.setCurrentWallet(wallet)
            asyncStateManager.accountAddress = cosmosAddress
            asyncStateManager.sourceAddress = ethereumAddress
        }
    }

    override fun logOut() {
        walletStateManager.clear()
        transferStateManager.clear()

        asyncStateManager.accountAddress = null
    }

    override fun replaceCurrentWallet() {
        walletStateManager.replaceWallet()
        if (walletStateManager.state.value?.currentWallet == null) {
            logOut()
        }
    }

    override fun setMarket(marketId: String?) {
        this.marketId.value = marketId
        if (marketId != asyncStateManager.market) {
            asyncStateManager.market = marketId
            asyncStateManager.trade(null, TradeInputField.size)
        }
    }

    override fun setCandlesPeriod(candlesPeriod: String) {
        this.candlesPeriod.value = candlesPeriod
        asyncStateManager.candlesResolution = candlesPeriod
    }

    override fun setHistoricalPNLPeriod(period: HistoricalPnlPeriod) {
        asyncStateManager.historicalPnlPeriod = period
    }

    override fun setHistoricalTradingRewardsPeriod(period: HistoricalTradingRewardsPeriod) {
        asyncStateManager.historicalTradingRewardPeriod = period
    }

    override fun startTrade() {
        asyncStateManager.trade(null, null)
    }

    override fun startClosePosition(marketId: String) {
        asyncStateManager.closePosition(marketId, ClosePositionInputField.market)
    }

    override fun startDeposit() {
        asyncStateManager.transfer("DEPOSIT", TransferInputField.type)
    }

    override fun startWithdrawal() {
        asyncStateManager.transfer("WITHDRAWAL", TransferInputField.type)
    }

    override fun startTransferOut() {
        asyncStateManager.transfer("TRANSFER_OUT", TransferInputField.type)
    }

    override fun trade(input: String?, type: TradeInputField?) {
        asyncStateManager.trade(input, type)
    }

    override fun closePosition(input: String?, type: ClosePositionInputField) {
        asyncStateManager.closePosition(input, type)
    }

    override fun transfer(input: String?, type: TransferInputField?) {
        asyncStateManager.transfer(input, type)
    }

    override fun faucet(
        amount: Int,
        statusCallback: (AbacusStateManagerProtocol.SubmissionStatus) -> Unit
    ) {
        asyncStateManager.faucet(amount.toDouble()) { successful, error, _ ->
            if (successful) {
                statusCallback(AbacusStateManagerProtocol.SubmissionStatus.Success)
            } else {
                statusCallback(AbacusStateManagerProtocol.SubmissionStatus.Failed(error))
            }
        }
    }

    override fun setOrderbookMultiplier(multiplier: OrderbookGrouping) {
        asyncStateManager.orderbookGrouping = multiplier
    }

    override fun placeOrder(statusCallback: (AbacusStateManagerProtocol.SubmissionStatus) -> Unit) {
        asyncStateManager.commitPlaceOrder { successful: Boolean, error: ParsingError?, data: Any? ->
            if (successful) {
                statusCallback(AbacusStateManagerProtocol.SubmissionStatus.Success)
            } else {
                statusCallback(AbacusStateManagerProtocol.SubmissionStatus.Failed(error))
            }
        }
    }

    override fun closePosition(statusCallback: (AbacusStateManagerProtocol.SubmissionStatus) -> Unit) {
        asyncStateManager.commitClosePosition { successful: Boolean, error: ParsingError?, data: Any? ->
            if (successful) {
                statusCallback(AbacusStateManagerProtocol.SubmissionStatus.Success)
            } else {
                statusCallback(AbacusStateManagerProtocol.SubmissionStatus.Failed(error))
            }
        }
    }

    override fun cancelOrder(
        orderId: String,
        statusCallback: (AbacusStateManagerProtocol.SubmissionStatus) -> Unit
    ) {
        asyncStateManager.cancelOrder(orderId) { successful: Boolean, error: ParsingError?, data: Any? ->
            if (successful) {
                statusCallback(AbacusStateManagerProtocol.SubmissionStatus.Success)
            } else {
                statusCallback(AbacusStateManagerProtocol.SubmissionStatus.Failed(error))
            }
        }
    }

    override fun transferStatus(
        hash: String,
        fromChainId: String?,
        toChainId: String?,
        isCctp: Boolean,
        requestId: String?
    ) {
        asyncStateManager.transferStatus(hash, fromChainId, toChainId, isCctp, requestId)
    }

    override fun screen(address: String, callback: ((Restriction) -> Unit)) {
        asyncStateManager.screen(address, callback)
    }

    override fun commitCCTPWithdraw(callback: (Boolean, ParsingError?, Any?) -> Unit) {
        asyncStateManager.commitCCTPWithdraw(callback)
    }

    override fun triggerOrders(input: String?, type: TriggerOrdersInputField?) {
        asyncStateManager.triggerOrders(input, type)
    }

    override fun commitTriggerOrders(callback: (AbacusStateManagerProtocol.SubmissionStatus) -> Unit) {
        asyncStateManager.commitTriggerOrders { successful: Boolean, error: ParsingError?, _ ->
            if (successful) {
                callback(AbacusStateManagerProtocol.SubmissionStatus.Success)
            } else {
                callback(AbacusStateManagerProtocol.SubmissionStatus.Failed(error))
            }
        }
    }

    override fun adjustIsolatedMargin(data: String?, type: AdjustIsolatedMarginInputField?) {
        asyncStateManager.adjustIsolatedMargin(data, type)
    }

    override fun commitAdjustIsolatedMargin(statusCallback: (AbacusStateManagerProtocol.SubmissionStatus) -> Unit) {
        asyncStateManager.commitAdjustIsolatedMargin { successful: Boolean, error: ParsingError?, data: Any? ->
            if (successful) {
                statusCallback(AbacusStateManagerProtocol.SubmissionStatus.Success)
            } else {
                statusCallback(AbacusStateManagerProtocol.SubmissionStatus.Failed(error))
            }
        }
    }

    override fun setGasToken(gasToken: GasToken) {
        asyncStateManager.gasToken = gasToken
    }

    override fun registerPushToken(token: String, language: String) {
        asyncStateManager.registerPushNotification(token, language)
    }

    override fun refreshVaultAccount() {
        asyncStateManager.refreshVaultAccount()
    }

    // MARK: StateNotificationProtocol

    override fun apiStateChanged(apiState: ApiState?) {
        apiStatePublisher.value = apiState
    }

    override fun environmentsChanged() {
        Handler(Looper.getMainLooper()).post {
            initializeCurrentEnvironment()
        }
    }

    override fun errorsEmitted(errors: IList<ParsingError>) {
        errorsStatePublisher.value = errors
    }

    override fun lastOrderChanged(order: SubaccountOrder?) {
        lastOrderPublisher.value = order
    }

    override fun notificationsChanged(notifications: IList<Notification>) {
        alertsPublisher.value = notifications
    }

    override fun stateChanged(state: PerpetualState?, changes: StateChanges?) {
        perpetualStatePublisher.value = state
        documentationPublisher.value = asyncStateManager.documentation
    }

    fun setReadyToConnect(readyToConnect: Boolean) {
        if (readyToConnect != asyncStateManager.readyToConnect) {
            asyncStateManager.readyToConnect = readyToConnect
        }
    }

    // MARK: Private

    private fun initializeCurrentEnvironment() {
        val stored = preferencesStore.read(envKey)
        val currentEnvironmentId = if (this.currentEnvironmentId.value == null && stored != null &&
            availableEnvironments.any { selection ->
                selection.type == stored
            }
        ) {
            stored
        } else {
            asyncStateManager.environmentId
        }
        setEnvironmentId(currentEnvironmentId)
    }

    private fun start() {
        appScope.launch {
            val currentWallet = walletStateManager.state.first()?.currentWallet
            if (currentWallet != null) {
                val ethereumAddress = currentWallet.ethereumAddress
                val cosmoAddress = currentWallet.cosmoAddress
                val mnemonic = currentWallet.mnemonic
                val walletId = currentWallet.walletId

                if (cosmoAddress != null && mnemonic != null) {
                    setV4(ethereumAddress, walletId, cosmoAddress, mnemonic)
                }
            }

            asyncStateManager.readyToConnect = true
        }
    }

    /*
    ConnectivityManager available after LOLLIPOP (API 21)
    Our minimum requirement is way higher
    */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun monitorConnectivity(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()

        open class NetworkCallback(
            private val abacusStateManager: SingletonAsyncAbacusStateManagerProtocol,
        ) : ConnectivityManager.NetworkCallback()

        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : NetworkCallback(abacusStateManager = asyncStateManager) {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    asyncStateManager.readyToConnect = true
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    asyncStateManager.readyToConnect = false
                }
            },
        )
    }
}

val V4Environment.usdcTokenInfo: TokenInfo?
    get() = tokens["usdc"]

val V4Environment.nativeTokenInfo: TokenInfo?
    get() = tokens["chain"]
