package exchange.dydx.trading.feature.transfer.search

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputResources

data class DydxTransferSearchParam(
    val options: List<SelectionOption>?,
    val selected: SelectionOption?,
    val resources: TransferInputResources?,
    val selectedCallback: (SelectionOption) -> Unit,
)
