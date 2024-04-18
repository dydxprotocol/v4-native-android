package exchange.dydx.trading

import android.app.Application
import android.content.Context
import androidx.compose.material.SnackbarHostState
import dagger.Binds
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
import exchange.dydx.dydxstatemanager.EnvKey
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
import exchange.dydx.dydxstatemanager.protocolImplementations.LanguageKey
import exchange.dydx.platformui.components.PlatformDialog
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.platformui.designSystem.theme.StyleConfig
import exchange.dydx.platformui.designSystem.theme.ThemeConfig
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.AppConfigImpl
import exchange.dydx.trading.common.di.CoroutineScopes
import exchange.dydx.trading.common.theme.DydxTheme
import exchange.dydx.trading.common.theme.DydxThemeImpl
import exchange.dydx.trading.feature.shared.PreferenceKeys
import exchange.dydx.trading.integration.analytics.CompositeTracker
import exchange.dydx.trading.integration.analytics.CompositeTracking
import exchange.dydx.trading.integration.analytics.Tracking
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientProtocol
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientWebview
import exchange.dydx.trading.integration.cosmos.CosmosV4WebviewClientProtocol
import exchange.dydx.utilities.utils.JsonUtils
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    companion object {
        @Provides
        @Singleton
        fun provideThemeSettings(
            @ApplicationContext appContext: Context,
            preferenceStore: SharedPreferencesStore,
        ): ThemeSettings {
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
                ThemeSettings(appContext, preferenceStore, themeConfig, styleConfig)
            return ThemeSettings.shared
        }

        @Provides @Singleton
        fun provideParser(): ParserProtocol = Parser()

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

        @EnvKey @Provides
        fun provideEnvKey(): String = PreferenceKeys.Env

        @LanguageKey @Provides
        fun provideLanguageKey(): String = PreferenceKeys.Language

        @Provides
        fun providePlatformInfo(
            @CoroutineScopes.App appScope: CoroutineScope,
        ): PlatformInfo =
            PlatformInfo(
                snackbarHostState = SnackbarHostState(),
                infoType = MutableStateFlow(PlatformInfo.InfoType.Info),
                appScope = appScope,
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
        fun provideAppConfig(
            application: Application,
        ): AppConfig = AppConfigImpl(
            appContext = application,
            appVersionName = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE.toString(),
            debug = BuildConfig.DEBUG,
            activityClass = TradingActivity::class.java,
        )

        @Provides
        @Singleton
        fun provideJson(appConfig: AppConfig): Json = Json {
            this.prettyPrint = appConfig.debug
            this.ignoreUnknownKeys = true // !appConfig.debug
        }

        @Provides
        fun provideTheme(
            application: Application,
            appConfig: AppConfig,
        ): DydxTheme {
            return DydxThemeImpl(application, appConfig, true)
        }
    }

    @Binds fun bindCosmosClient(client: CosmosV4WebviewClientProtocol): CosmosV4ClientProtocol

    @Binds fun bindRest(abacusRestImp: AbacusRestImp): RestProtocol

    @Binds fun bindWebsocket(abacusWebSocketImp: AbacusWebSocketImp): WebSocketProtocol

    @Binds fun bindChainProtocol(abacusChainImp: AbacusChainImp): DYDXChainTransactionsProtocol

    @Binds fun bindTrackingProtocol(abacusTrackingImp: AbacusTrackingImp): TrackingProtocol

    @Binds fun bindTracking(compositeTracking: CompositeTracking): Tracking

    @Binds fun bindThreading(abacusThreadingImp: AbacusThreadingImp): ThreadingProtocol

    @Binds fun bindTimer(abacusTimerImp: AbacusTimerImp): TimerProtocol

    @Binds fun bindFileSystem(abacusFileSystemImp: AbacusFileSystemImp): FileSystemProtocol

    @Binds fun bindAbacusStateManager(abacusStateManager: AbacusStateManager): AbacusStateManagerProtocol

    @Binds fun bindLocalizer(abacusLocalizerProtocol: AbacusLocalizerProtocol): LocalizerProtocol

    @Binds
    fun bindLocalizerProtocol(abacusLocalizerImp: AbacusLocalizerImp): AbacusLocalizerProtocol

    @Binds
    fun bindWalletStateManagerProtocol(walletStateManager: DydxWalletStateManager): DydxWalletStateManagerProtocol

    @Binds
    fun bindTransferStateManagerProtocol(dydxTransferStateManager: DydxTransferStateManager): DydxTransferStateManagerProtocol

    @Binds
    fun bindUserFavoriteStoreProtocol(dydxFavoriteStore: DydxFavoriteStore): DydxFavoriteStoreProtocol

    @Binds
    fun bindCompositeTracking(compositeTracker: CompositeTracker): CompositeTracking

    @Binds fun bindCosmosV4WebviewClientProtocol(cosmosV4ClientWebview: CosmosV4ClientWebview): CosmosV4WebviewClientProtocol
}
