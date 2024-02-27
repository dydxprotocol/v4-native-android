package exchange.dydx.trading

import android.content.Context
import androidx.compose.material.SnackbarHostState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.abacus.protocols.DYDXChainTransactionsProtocol
import exchange.dydx.abacus.protocols.FileSystemProtocol
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.RestProtocol
import exchange.dydx.abacus.protocols.ThreadingProtocol
import exchange.dydx.abacus.protocols.TimerProtocol
import exchange.dydx.abacus.protocols.TrackingProtocol
import exchange.dydx.abacus.protocols.WebSocketProtocol
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.dydxstatemanager.AbacusStateManager
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.DydxClientState
import exchange.dydx.dydxstatemanager.clientState.favorite.DydxFavoriteStore
import exchange.dydx.dydxstatemanager.clientState.favorite.DydxFavoriteStoreProtocol
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferStateManager
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletStateManager
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletStateManagerProtocol
import exchange.dydx.dydxstatemanager.protocolImplementations.AbacusChainImp
import exchange.dydx.dydxstatemanager.protocolImplementations.AbacusFileSystemImp
import exchange.dydx.dydxstatemanager.protocolImplementations.AbacusLocalizerImp
import exchange.dydx.dydxstatemanager.protocolImplementations.AbacusRestImp
import exchange.dydx.dydxstatemanager.protocolImplementations.AbacusThreadingImp
import exchange.dydx.dydxstatemanager.protocolImplementations.AbacusTimerImp
import exchange.dydx.dydxstatemanager.protocolImplementations.AbacusTrackingImp
import exchange.dydx.dydxstatemanager.protocolImplementations.AbacusWebSocketImp
import exchange.dydx.platformui.components.PlatformDialog
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.platformui.designSystem.theme.StyleConfig
import exchange.dydx.platformui.designSystem.theme.ThemeConfig
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.AppConfigImpl
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.common.logger.DydxLogger
import exchange.dydx.trading.common.theme.DydxTheme
import exchange.dydx.trading.common.theme.DydxThemeImpl
import exchange.dydx.trading.feature.shared.PreferenceKeys
import exchange.dydx.trading.integration.analytics.CompositeTracker
import exchange.dydx.trading.integration.analytics.CompositeTracking
import exchange.dydx.trading.integration.analytics.Tracking
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientProtocol
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientWebview
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.CachedFileLoader
import exchange.dydx.utilities.utils.JsonUtils
import exchange.dydx.utilities.utils.SecureStore
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideThemeSettings(
        @ApplicationContext appContext: Context,
        sharedPreferencesStore: SharedPreferencesStore,
    ): ThemeSettings {
        val preferenceStore = provideSharedPreferencesStore(appContext)
        var theme = preferenceStore.read(PreferenceKeys.Theme)
        if (theme.isNullOrEmpty()) {
            theme = "dark"
        }
        val themeConfigValue =
            ThemeConfig.createFromPreference(appContext, theme) ?: ThemeConfig.dark(appContext)
        val themeConfig = MutableStateFlow<ThemeConfig?>(themeConfigValue)
        val styleConfig =
            MutableStateFlow<StyleConfig?>(JsonUtils.loadFromAssets(appContext, "dydxStyle.json"))
        ThemeSettings.shared =
            ThemeSettings(appContext, sharedPreferencesStore, themeConfig, styleConfig)
        return ThemeSettings.shared
    }

    @Provides
    @Singleton
    fun provideParser(): ParserProtocol =
        Parser()

    @Provides
    @Singleton
    fun provideSharedPreferencesStore(@ApplicationContext appContext: Context): SharedPreferencesStore =
        SharedPreferencesStore(appContext)

    @Provides
    @Singleton
    fun provideSecureStore(@ApplicationContext appContext: Context): SecureStore =
        SecureStore(appContext)

    @Provides
    @Singleton
    fun provideClientState(
        sharedPreferencesStore: SharedPreferencesStore,
        secureStore: SecureStore,
    ): DydxClientState =
        DydxClientState(sharedPreferencesStore, secureStore)

    @Provides
    @Singleton
    fun provideCosmosClient(client: CosmosV4WebviewClientProtocol): CosmosV4ClientProtocol =
        client

    @Provides
    @Singleton
    fun provideCosmosWebviewClient(@ApplicationContext appContext: Context): CosmosV4WebviewClientProtocol =
        CosmosV4ClientWebview(appContext)

    @Provides
    @Singleton
    fun provideRest(): RestProtocol =
        AbacusRestImp()

    @Provides
    @Singleton
    fun provideWebsocket(): WebSocketProtocol =
        AbacusWebSocketImp()

    @Provides
    @Singleton
    fun provideChain(cosmosClient: CosmosV4ClientProtocol): DYDXChainTransactionsProtocol =
        AbacusChainImp(cosmosClient)

    @Provides
    @Singleton
    fun provideTrackingProtocol(
        tracker: Tracking,
    ): TrackingProtocol =
        tracker

    @Provides
    @Singleton
    fun provideTracking(
        tracker: CompositeTracking,
    ): Tracking =
        AbacusTrackingImp(tracker)

    @Provides
    @Singleton
    fun provideCompositeTracker(): CompositeTracking =
        CompositeTracker()

    @Provides
    @Singleton
    fun provideThreading(): ThreadingProtocol =
        AbacusThreadingImp()

    @Provides
    @Singleton
    fun provideTimer(): TimerProtocol =
        AbacusTimerImp()

    @Provides
    @Singleton
    fun provideFileSystem(@ApplicationContext appContext: Context): FileSystemProtocol =
        AbacusFileSystemImp(appContext)

    @Provides
    @Singleton
    fun provideIOImplementation(
        rest: RestProtocol?,
        webSocket: WebSocketProtocol?,
        chain: DYDXChainTransactionsProtocol?,
        tracking: TrackingProtocol?,
        threading: ThreadingProtocol?,
        timer: TimerProtocol?,
        fileSystem: FileSystemProtocol?,
    ): IOImplementations =
        IOImplementations(rest, webSocket, chain, tracking, threading, timer, fileSystem)

    @Provides
    @Singleton
    fun provideAbacusStateManager(
        @ApplicationContext appContext: Context,
        ioImplementations: IOImplementations,
        parser: ParserProtocol,
        walletStateManager: DydxWalletStateManagerProtocol,
        transferStateManager: DydxTransferStateManagerProtocol,
        cosmosClient: CosmosV4ClientProtocol,
        preferencesStore: SharedPreferencesStore,
        featureFlags: DydxFeatureFlags,
    ): AbacusStateManagerProtocol =
        AbacusStateManager(
            appContext,
            ioImplementations,
            parser,
            walletStateManager,
            transferStateManager,
            cosmosClient,
            preferencesStore,
            PreferenceKeys.Env,
            featureFlags,
        )

    @Provides
    @Singleton
    fun provideFeatureFlags(preferencesStore: SharedPreferencesStore): DydxFeatureFlags =
        DydxFeatureFlags(preferencesStore)

    @Provides
    @Singleton
    fun provideLocalizer(
        abacusLocalizerProtocol: AbacusLocalizerProtocol,
    ): LocalizerProtocol =
        abacusLocalizerProtocol

    @Provides
    @Singleton
    fun provideMutableLocalizer(
        preferencesStore: SharedPreferencesStore,
        ioImplementations: IOImplementations,
    ): AbacusLocalizerProtocol =
        AbacusLocalizerImp(preferencesStore, PreferenceKeys.Language, ioImplementations)

    @Provides
    @Singleton
    fun provideWalletStateManager(clientState: DydxClientState): DydxWalletStateManagerProtocol =
        DydxWalletStateManager(clientState)

    @Provides
    @Singleton
    fun provideTransferStateManager(clientState: DydxClientState): DydxTransferStateManagerProtocol =
        DydxTransferStateManager(clientState)

    @Provides
    @Singleton
    fun provideUserFavoriteStore(clientState: DydxClientState): DydxFavoriteStoreProtocol =
        DydxFavoriteStore(clientState)

    @Provides
    fun providePlatformInfo(): PlatformInfo =
        PlatformInfo(
            snackbarHostState = SnackbarHostState(),
            infoType = MutableStateFlow(PlatformInfo.InfoType.Info),
        )

    @Provides
    fun providePlatformDialog(
        localizer: LocalizerProtocol,
    ): PlatformDialog =
        PlatformDialog(
            cancelTitle = localizer.localize("APP.GENERAL.CANCEL"),
            confirmTitle = localizer.localize("APP.GENERAL.OK"),
        )

    @Provides
    @Singleton
    fun provideCachedFileLoader(
        @ApplicationContext appContext: Context,
    ): CachedFileLoader {
        return CachedFileLoader(appContext)
    }

    @Provides
    @Singleton
    fun provideAppConfig(
        @ApplicationContext appContext: Context,
    ): AppConfig = AppConfigImpl(
        appContext = appContext,
        appVersionName = BuildConfig.VERSION_NAME,
        appVersionCode = BuildConfig.VERSION_CODE.toString(),
        debug = BuildConfig.DEBUG,
        activityClass = TradingActivity::class.java,
    )

    @Provides
    @Singleton
    fun provideLogger(): DydxLogger = DydxLogger()

    @Provides
    @Singleton
    fun provideJson(appConfig: AppConfig): Json = Json {
        this.prettyPrint = appConfig.debug
        this.ignoreUnknownKeys = true // !appConfig.debug
    }

    @Provides
    @Singleton
    fun provideTheme(
        @ApplicationContext appContext: Context,
        appConfig: AppConfig,
    ): DydxTheme {
        return DydxThemeImpl(appContext, appConfig, true)
    }
}
