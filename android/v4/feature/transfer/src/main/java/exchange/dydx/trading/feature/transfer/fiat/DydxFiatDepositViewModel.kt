package exchange.dydx.trading.feature.transfer.fiat

import android.R.attr.value
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.localizeWithParams
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.AppConfig
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.featureflags.RemoteFlags
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxFiatDepositViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val router: DydxRouter,
    private val appConfig: AppConfig,
    private val formatter: DydxFormatter,
    private val remoteFlags: RemoteFlags,
) : ViewModel(), DydxViewModel {

    private val valueState = MutableStateFlow("")

    val state: Flow<DydxFiatDepositView.ViewState?> =  valueState
        .map { currentValue ->
            createViewState(currentValue)
        }
        .distinctUntilChanged()

    private fun createViewState(
        currentValue: String
    ): DydxFiatDepositView.ViewState {
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
            },
            ctaEnabled = currentValueDouble >= minAmount,
            providerName = "MoonPay",
            providerIcon = R.drawable.icon_moonpay,
            providerSubtitle = localizer.localize("APP.DEPOSIT_WITH_FIAT.MOONPAY_SUPPORT"),
            fee = formatter.percent(feePercent / 100, digits = 2),
            amountSubtitle = localizer.localizeWithParams(
                path = "APP.DEPOSIT_WITH_FIAT.MINIMUM_MOONPAY_DEPOSIT",
                params = mapOf("MIN" to (minDollar ?: "-")),
            ),
        )
    }
}
