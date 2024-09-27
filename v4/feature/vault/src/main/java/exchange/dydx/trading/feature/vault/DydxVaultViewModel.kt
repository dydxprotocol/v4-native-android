package exchange.dydx.trading.feature.vault

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.VaultPosition
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.vault.components.DydxVaultPositionItemView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class DydxVaultViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val abacusStateManager: AbacusStateManagerProtocol,
    private val formatter: DydxFormatter,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxVaultView.ViewState?> =
        combine(
            abacusStateManager.state.vault,
            abacusStateManager.state.marketMap,
            abacusStateManager.state.assetMap,
        ) { vault, marketMap, assetMap ->
            createViewState(vault, marketMap, assetMap)
        }
            .distinctUntilChanged()

    private fun createViewState(
        vault: Vault?,
        marketMap: Map<String, PerpetualMarket>?,
        assetMap: Map<String, Asset>?,
    ): DydxVaultView.ViewState {
        val items: List<DydxVaultPositionItemView.ViewState> = vault?.positions?.positions?.mapNotNull { position ->
            val marketId = position.marketId ?: return@mapNotNull null
            val market = marketMap?.get(marketId) ?: return@mapNotNull null
            val asset = assetMap?.get(market.assetId) ?: return@mapNotNull null
            createPositionItem(position, asset)
        } ?: listOf()
        return DydxVaultView.ViewState(
            localizer = localizer,
            items = items,
        )
    }

    private fun createPositionItem(
        position: VaultPosition,
        asset: Asset
    ): DydxVaultPositionItemView.ViewState? {
        val marketId = position.marketId ?: return null
        return DydxVaultPositionItemView.ViewState(
            localizer = localizer,
            id = marketId,
            logoUrl = asset.resources?.imageUrl,
            assetName = asset.name,
            market = marketId,
            side = SideTextView.ViewState(
                localizer = localizer,
                side = position.side,
            ),
            leverage = formatter.raw(position.currentLeverageMultiple?.absoluteValue, digits = 2),
            notionalValue = formatter.dollar(position.currentPosition?.usdc, digits = 0),
            positionSize = formatter.raw(position.currentPosition?.asset, digits = 2),
            token = TokenTextView.ViewState(
                symbol = asset.id,
            ),
            pnlAmount = if (position.thirtyDayPnl?.absolute != null) {
                SignedAmountView.ViewState(
                    sign = position.pnlSign,
                    text = formatter.dollar(position.thirtyDayPnl?.absolute, digits = 0) ?: "-",
                )
            } else {
                SignedAmountView.ViewState(
                    sign = PlatformUISign.None,
                    text = "-",
                )
            },
            pnlPercentage = formatter.percent(position.thirtyDayPnl?.percent, digits = 2),
        )
    }
}

private val VaultPosition.side: SideTextView.Side
    get() = run {
        val size = this.currentPosition?.asset ?: return SideTextView.Side.None
        return if (size > 0) {
            SideTextView.Side.Long
        } else if (size < 0) {
            SideTextView.Side.Short
        } else {
            SideTextView.Side.None
        }
    }

private val VaultPosition.pnlSign: PlatformUISign
    get() = run {
        val pnl = this.thirtyDayPnl?.absolute ?: 0.0
        return if (pnl > 0) {
            PlatformUISign.Plus
        } else if (pnl < 0) {
            PlatformUISign.Minus
        } else {
            PlatformUISign.None
        }
    }
