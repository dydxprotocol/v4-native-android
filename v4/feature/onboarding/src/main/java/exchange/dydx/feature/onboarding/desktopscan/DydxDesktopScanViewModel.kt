package exchange.dydx.feature.onboarding.desktopscan

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.integration.starkex.StarkexLib
import exchange.dydx.platformui.components.PlatformDialog
import exchange.dydx.platformui.components.PlatformInfo
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

@HiltViewModel
class DydxDesktopScanViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private val parser: ParserProtocol,
    val platformDialog: PlatformDialog,
    val platformInfo: PlatformInfo,
    val starkexLib: StarkexLib,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxDesktopScanView.ViewState?> = abacusStateManager.state.marketSummary
        .map {
            createViewState(it)
        }
        .distinctUntilChanged()

    private fun createViewState(marketSummary: PerpetualMarketSummary?): DydxDesktopScanView.ViewState {
        val volume = formatter.dollarVolume(marketSummary?.volume24HUSDC)
        return DydxDesktopScanView.ViewState(
            localizer = localizer,
            text = volume,
            closeButtonHandler = {
                router.navigateBack()
            },
            qrCodeScannedHandler = { value ->
                platformDialog.showTextEntry(
                    title = localizer.localize("APP.ONBOARDING.ENTER_CODE"),
                    cancelTitle = localizer.localize("APP.GENERAL.CANCEL"),
                    confirmTitle = localizer.localize("APP.GENERAL.OK"),
                    confirmAction = { key ->
                        if (key != null) {
                            decryptQrPayload(value, key)
                        }
                    },
                )
            },
        )
    }

    private fun decryptQrPayload(encryptedString: String, encryptKey: String) {
        starkexLib.aesDecrypt(encryptedString, encryptKey) { decrypted, error ->
            if (error != null) {
                platformInfo.show(
                    message = localizer.localize("APP.ONBOARDING.SCAN_QR_CODE_ERROR"),
                )
            } else if (decrypted != null) {
                val json = Json.parseToJsonElement(decrypted)
                val map = json.jsonObject.toMap()
                val mnemonic = parser.asString(map["mnemonic"])
                val cosmosAddress = parser.asString(map["cosmosAddress"])
                if (mnemonic != null && cosmosAddress != null) {
                    abacusStateManager.setV4(
                        ethereumAddress = cosmosAddress,
                        mnemonic = mnemonic,
                        cosmosAddress = cosmosAddress,
                        walletId = null,
                    )
                    router.navigateBack()
                    router.navigateTo(PortfolioRoutes.main)
                } else {
                    platformInfo.show(
                        message = localizer.localize("APP.ONBOARDING.SCAN_QR_CODE_ERROR"),
                    )
                }
            }
        }
    }
}
