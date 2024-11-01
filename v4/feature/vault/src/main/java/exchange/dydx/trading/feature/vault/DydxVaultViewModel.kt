package exchange.dydx.trading.feature.vault

import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.functional.vault.ThirtyDayPnl
import exchange.dydx.abacus.functional.vault.VaultPosition
import exchange.dydx.abacus.functional.vault.VaultPositions
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.Vault
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.PlatformUISign
import exchange.dydx.platformui.components.charts.view.LineChartDataSet
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SideTextView.Side
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.SparklineView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.vault.components.DydxVaultPositionItemView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.UUID
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
        val items: List<DydxVaultPositionItemView.ViewState> = vault?.positions?.sortedByEquity?.mapNotNull { position ->
            val marketId = position.marketId ?: return@mapNotNull null
            if (marketId == "UNALLOCATEDUSDC-USD") {
                return@mapNotNull createUsdcItem(position)
            }
            val market = marketMap?.get(marketId)
            val asset = market?.assetId?.let { assetMap?.get(it) }
            createPositionItem(position, asset)
        } ?: listOf()
        return DydxVaultView.ViewState(
            localizer = localizer,
            items = items,
        )
    }

    private fun createUsdcItem(
        position: VaultPosition,
    ): DydxVaultPositionItemView.ViewState? {
        val marketId = position.marketId ?: return null
        return DydxVaultPositionItemView.ViewState(
            localizer = localizer,
            id = marketId + UUID.randomUUID(),
            logoUrl = null,
            assetName = "USDC",
            market = marketId,
            side = SideTextView.ViewState(
                localizer = localizer,
                side = Side.Long,
            ),
            leverage = "1.00x",
            notionalValue = formatter.dollarVolume(
                (position.currentPosition?.usdc?.absoluteValue ?: 0.0),
                digits = 2,
            ),
            equity = formatter.dollarVolume(
                (position.marginUsdc?.absoluteValue ?: 0.0),
                digits = 0,
            ),
            positionSize = formatter.condensed(
                (position.currentPosition?.asset?.absoluteValue ?: 0.0),
                digits = 0,
            ),
            token = TokenTextView.ViewState(
                symbol = "USDC",
            ),
            pnlAmount = SignedAmountView.ViewState(
                sign = position.pnlSign,
                text = formatter.dollarVolume(0.0, digits = 2) ?: "-",
            ),
            pnlPercentage = formatter.percent(0.0, digits = 2),
            sparkline = createSparkline(position.thirtyDayPnl),
        )
    }

    private fun createPositionItem(
        position: VaultPosition,
        asset: Asset?
    ): DydxVaultPositionItemView.ViewState? {
        val marketId = position.marketId ?: return null
        return DydxVaultPositionItemView.ViewState(
            localizer = localizer,
            id = marketId,
            logoUrl = asset?.resources?.imageUrl,
            assetName = asset?.name,
            market = marketId,
            side = SideTextView.ViewState(
                localizer = localizer,
                side = position.side,
            ),
            leverage = formatter.raw(position.currentLeverageMultiple?.absoluteValue, digits = 2)
                ?.let {
                    "${it}x"
                },
            notionalValue = formatter.dollarVolume(
                (position.currentPosition?.usdc?.absoluteValue ?: 0.0),
                digits = 2,
            ),
            equity = formatter.dollarVolume(
                (position.marginUsdc?.absoluteValue ?: 0.0),
                digits = 0,
            ),
            positionSize = formatter.condensed(
                (position.currentPosition?.asset?.absoluteValue ?: 0.0),
                digits = 0,
            ),
            token = asset?.displayableAssetId?.let {
                TokenTextView.ViewState(
                    symbol = it,
                )
            },
            pnlAmount = if (position.thirtyDayPnl?.absolute != null) {
                SignedAmountView.ViewState(
                    sign = position.pnlSign,
                    text = formatter.dollarVolume(
                        position.thirtyDayPnl?.absolute?.absoluteValue,
                        digits = 2,
                    ) ?: "-",
                )
            } else {
                SignedAmountView.ViewState(
                    sign = PlatformUISign.None,
                    text = "-",
                )
            },
            pnlPercentage = formatter.percent(position.thirtyDayPnl?.percent, digits = 2),
            sparkline = createSparkline(position.thirtyDayPnl),
        )
    }

    private fun createSparkline(pnl: ThirtyDayPnl?): SparklineView.ViewState? {
        val pnl = pnl ?: return null
        var x = -1
        val lines = pnl.sparklinePoints?.map {
            x += 1
            Entry(x.toFloat(), it.toFloat())
        } ?: emptyList()
        return if (lines.isNotEmpty()) {
            val total = pnl.absolute ?: 0.0
            SparklineView.ViewState(
                sparkline = LineChartDataSet(lines, "Sparkline"),
                sign = PlatformUISign.from(total),
            )
        } else {
            null
        }
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

private val VaultPositions.sortedBySize: List<VaultPosition>?
    get() = this.positions?.sortedWith { p1, p2 ->
        val size1 = p1.currentPosition?.usdc ?: 0.0
        val size2 = p2.currentPosition?.usdc ?: 0.0
        if (size1 == size2) {
            p2.thirtyDayPnl?.absolute?.compareTo(p1.thirtyDayPnl?.absolute ?: 0.0) ?: 0
        } else {
            size2.compareTo(size1)
        }
    }

private val VaultPositions.sortedByEquity: List<VaultPosition>?
    get() = this.positions?.sortedWith { p1, p2 ->
        val size1 = p1.marginUsdc ?: 0.0
        val size2 = p2.marginUsdc ?: 0.0
        if (size1 == size2) {
            p2.thirtyDayPnl?.absolute?.compareTo(p1.thirtyDayPnl?.absolute ?: 0.0) ?: 0
        } else {
            size2.compareTo(size1)
        }
    }
