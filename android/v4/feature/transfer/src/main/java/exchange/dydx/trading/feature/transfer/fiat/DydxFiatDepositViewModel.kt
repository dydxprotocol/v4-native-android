package exchange.dydx.trading.feature.transfer.fiat

import android.R.attr.value
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.localizeWithParams
import exchange.dydx.abacus.utils.AbacusStringUtils
import exchange.dydx.dydxfiatramp.DydxMoonPayConfig
import exchange.dydx.dydxfiatramp.DydxMoonPayRamp
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.dydxstatemanager.clientState.wallets.DydxWalletInstance
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.RemoteFlags
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.PreferenceKeys
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.utilities.utils.SharedPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class DydxFiatDepositViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val appConfig: AppConfig,
    private val formatter: DydxFormatter,
    private val remoteFlags: RemoteFlags,
    private val moonPayRamp: DydxMoonPayRamp,
    private val preferencesStore: SharedPreferencesStore,
    @ApplicationContext private val appContext: android.content.Context,
    private val platformInfo: PlatformInfo,
) : ViewModel(), DydxViewModel {

    private val valueState = MutableStateFlow("")

    val state: Flow<DydxFiatDepositView.ViewState?> =
        combine(
            valueState,
            abacusStateManager.state.currentWallet,
        ) { currentValue, currentWallet ->
            createViewState(currentValue, currentWallet)
        }
            .distinctUntilChanged()

    private fun createViewState(
        currentValue: String,
        currentWallet: DydxWalletInstance?
    ): DydxFiatDepositView.ViewState {
        val cosmosAddress = currentWallet?.cosmoAddress
        val nobleAddress = if (cosmosAddress != null) {
            AbacusStringUtils.toNobleAddress(cosmosAddress)
        } else {
            null
        }

        val feePercent = remoteFlags.getParamStoreValue("moonpay_fee_percent", 0.0)
        val minAmount = remoteFlags.getParamStoreValue("moonpay_min_deposit", 0.0)

        val minDollar = formatter.dollar(minAmount, digits = 2)
        val currentValueDouble = currentValue.toDoubleOrNull() ?: 0.0
        return DydxFiatDepositView.ViewState(
            localizer = localizer,
            formatter = formatter,
            value = currentValue,
            onEditAction = { value ->
                valueState.value = value
            },
            backButtonAction = {
                router.navigateBack()
            },
            ctaAction = {
                if (nobleAddress == null || currentValueDouble < minAmount) {
                    return@ViewState
                }
                showMoonPayUI(targetAddress = nobleAddress, usdAmount = currentValueDouble)
            },
            ctaEnabled = currentValueDouble >= minAmount,
            providerName = "MoonPay",
            providerIcon = R.drawable.icon_moonpay,
            providerSubtitle = localizer.localize("APP.DEPOSIT_WITH_FIAT.MOONPAY_SUPPORT_ANDROID"),
            fee = formatter.percent(feePercent / 100, digits = 2),
            amountSubtitle = localizer.localizeWithParams(
                path = "APP.DEPOSIT_WITH_FIAT.MINIMUM_MOONPAY_DEPOSIT",
                params = mapOf("MIN" to (minDollar ?: "-")),
            ),
        )
    }

    private fun showMoonPayUI(targetAddress: String, usdAmount: Double) {
        val theme = preferencesStore.read(key = PreferenceKeys.Theme, defaultValue = "dark")

        val config = DydxMoonPayConfig(
            isSandbox = !abacusStateManager.state.isMainNet,
            moonPayPk = appContext.getString(exchange.dydx.trading.common.R.string.moonpay_pk),
            moonPaySk = appContext.getString(exchange.dydx.trading.common.R.string.moonpay_sk),
            moonPaySignUrl = appContext.getString(exchange.dydx.trading.common.R.string.moonpay_sign_url),
            isDarkTheme = if (theme == "dark") true else false,
        )
        moonPayRamp.show(
            targetAddress = targetAddress,
            usdAmount = usdAmount,
            config = config,
            completion = { success, error ->
                if (success) {
                    router.navigateToRoot(excludeRoot = false)
                } else {
                    platformInfo.show(
                        title = localizer.localize("APP.GENERAL.ERROR"),
                        message = error,
                        type = PlatformInfoViewModel.Type.Error,
                    )
                }
            },
        )
    }
}
