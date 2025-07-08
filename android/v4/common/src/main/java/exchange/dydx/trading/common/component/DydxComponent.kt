package exchange.dydx.trading.common.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface DydxComponent {
    companion object {
        val None = object : DydxComponent {
            @Composable
            override fun Content(modifier: Modifier) {}
        }
    }

    @Composable
    fun Content(modifier: Modifier)
}
