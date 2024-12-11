package exchange.dydx.feature.onboarding.walletlist

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.cartera.CarteraConfig
import exchange.dydx.cartera.entities.installed
import exchange.dydx.dydxCartera.imageUrl
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.feature.onboarding.walletlist.components.DydxWalletListItemView
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.OnboardingRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DydxWalletListViewModel @Inject constructor(
    val appConfig: AppConfig,
    val localizer: LocalizerProtocol,
    val router: DydxRouter,
    val abacusStateManager: AbacusStateManagerProtocol,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), DydxViewModel {

    private var context: Context? = null
    private val mobileOnly: Boolean = savedStateHandle["mobileOnly"] ?: false

    private val _state = MutableStateFlow(DydxWalletListView.ViewState(localizer))
    val state: Flow<DydxWalletListView.ViewState> = _state

    fun updateContext(context: Context) {
        if (context != this.context) {
            this.context = context
            updateWalletList()
        }
    }

    private fun updateWalletList() {
        val context = this.context
        if (context != null) {
            val listState: MutableList<DydxWalletListItemView.ViewState> = mutableListOf()
            val wallets = CarteraConfig.shared?.wallets ?: listOf()
            for (wallet in wallets) {
                val installedText = if (wallet.installed(context)) {
                    localizer.localize("APP.GENERAL.INSTALLED")
                } else {
                    localizer.localize("APP.GENERAL.INSTALL")
                }
                val folder = abacusStateManager.environment?.walletConnection?.images
                val iconUrl = wallet.imageUrl(folder)
                listState.add(
                    DydxWalletListItemView.ViewState(
                        iconUrl = iconUrl,
                        main = wallet.metadata?.shortName ?: "",
                        trailing = installedText,
                        onTap = {
                            if (wallet.installed(context)) {
                                router.navigateBack()
                                router.navigateTo(
                                    route = OnboardingRoutes.connect + "/${wallet.id}",
                                    presentation = DydxRouter.Presentation.Modal,
                                )
                            } else {
                                wallet.app?.android?.let { android ->
                                    router.navigateBack()
                                    router.navigateTo(android)
                                }
                            }
                        },
                    ),
                )
            }
            _state.value = DydxWalletListView.ViewState(
                localizer = localizer,
                desktopSync = if (!mobileOnly) desktopSync else null,
                debugScan = if (!mobileOnly) debugScan else null,
                wcModal = wcModal,
                wallets = listState,
                backButtonHandler = {
                    router.navigateBack()
                },
            )
        }
    }

    private val wcModal: DydxWalletListItemView.ViewState by lazy {
        DydxWalletListItemView.ViewState(
            iconUrl = exchange.dydx.trading.feature.shared.R.drawable.icon_wc_logo,
            main = localizer.localize("APP.WALLETS.WALLET_CONNECT_2"),
            trailing = localizer.localize("APP.GENERAL.RECOMMENDED"),
            onTap = {
                router.navigateBack()
                router.navigateTo(
                    route = OnboardingRoutes.connect + "/walletconnect_modal",
                    presentation = DydxRouter.Presentation.Modal,
                )
            },
        )
    }

    private val desktopSync: DydxWalletListItemView.ViewState by lazy {
        DydxWalletListItemView.ViewState(
            iconUrl = exchange.dydx.trading.feature.shared.R.drawable.icon_qrcode,
            main = localizer.localize("APP.GENERAL.SYNC_WITH_DESKTOP"),
            trailing = localizer.localize("APP.ONBOARDING.SCAN_QR_CODE"),
            onTap = {
                router.navigateBack()
                router.navigateTo(
                    route = OnboardingRoutes.desktop_scan,
                    presentation = DydxRouter.Presentation.Push,
                )
            },
        )
    }

    private val debugScan: DydxWalletListItemView.ViewState? by lazy {
        if (appConfig.debug) {
            DydxWalletListItemView.ViewState(
                iconUrl = exchange.dydx.trading.feature.shared.R.drawable.icon_qrcode,
                main = "Scan me in Wallet",
                trailing = "Debug only",
                onTap = {
                    router.navigateBack()
                    router.navigateTo(
                        route = OnboardingRoutes.debug_scan,
                        presentation = DydxRouter.Presentation.Push,
                    )
                },
            )
        } else {
            null
        }
    }
}
