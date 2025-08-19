package exchange.dydx.trading.feature.transfer.deposit

import android.R.attr.action
import android.R.attr.subtitle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.trading.feature.shared.TransferChain
import exchange.dydx.trading.feature.shared.TransferTokenDetails
import exchange.dydx.trading.feature.shared.TransferTokenInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DydxTransferTurnkeyDepositViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
    private val router: DydxRouter,
    private val transferTokenDetails: TransferTokenDetails,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxTransferTurnkeyDepositView.ViewState?> =
        transferTokenDetails.infos
            .map { tokenInfos ->
                createViewState(tokenInfos = tokenInfos)
            }
            .distinctUntilChanged()

    private fun createViewState(
        tokenInfos: List<TransferTokenInfo>
    ): DydxTransferTurnkeyDepositView.ViewState {
        val chainOrders: List<TransferChain> = listOf(
            TransferChain.Solana,
            TransferChain.Ethereum,
            TransferChain.Arbitrum,
            TransferChain.Base,
            TransferChain.Optimism,
            TransferChain.Avalanche,
        )
        return DydxTransferTurnkeyDepositView.ViewState(
            localizer = localizer,
            closeAction = {
                router.navigateBack()
            },
            items = chainOrders.mapNotNull { tokenInfo ->
                val chain = tokenInfos.firstOrNull { it.chain == tokenInfo }
                chain?.let { createItem(it) }
            },
        )
    }

    private fun createItem(
        tokenInfo: TransferTokenInfo,
    ): DydxTransferTurnkeyDepositView.Item {
        return DydxTransferTurnkeyDepositView.Item(
            title = tokenInfo.chain.name,
            subtitle = tokenInfo.chain.supportedDepositTokenString,
            tag = tokenInfo.chain.depositFeesString(localizer),
            iconUrl = tokenInfo.chainLogUrl(abacusStateManager.deploymentUri),
            action = {
                // Router.shared?.navigate(to: RoutingRequest(path: "/transfer/deposit/qr_code", params: ["chain": tokenInfo.chain.rawValue]), animated: true, completion: nil)
            },
        )
    }
}
