package exchange.dydx.trading.feature.market.marketinfo.components.trades

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

object DydxMarketTradeLayout {
    @Composable
    fun Content(
        modifier: Modifier,
        colume1: @Composable (modifier: Modifier) -> Unit,
        colume2: @Composable (modifier: Modifier) -> Unit,
        colume3: @Composable (modifier: Modifier) -> Unit,
        colume4: @Composable (modifier: Modifier) -> Unit,
        background: (@Composable (modifier: Modifier) -> Unit)? = null,
    ) {
        Box(
            modifier = modifier,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .zIndex(0f),
            ) {
                background?.invoke(Modifier.fillMaxSize())
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 0.dp)
                    .zIndex(1f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.width(80.dp),
                    ) {
                        colume1(Modifier)
                    }

                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        colume2(Modifier)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        colume3(Modifier)
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.width(80.dp),
                    ) {
                        colume4(Modifier)
                    }
                }
            }
        }
    }
}
