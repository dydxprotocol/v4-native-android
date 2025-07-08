package exchange.dydx.trading.feature.profile.rewards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

object DydxRewardsLayout {
    @Composable
    fun Content(
        modifier: Modifier,
        colume1: @Composable (modifier: Modifier) -> Unit,
        colume2: @Composable (modifier: Modifier) -> Unit,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                colume1(Modifier)
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                colume2(Modifier)
            }
        }
    }
}
