package exchange.dydx.trading.feature.transfer.utils

import kotlinx.coroutines.flow.MutableStateFlow

enum class TransferRouteSelection {
    Instant, Regular
}

class TransferRouteSelectionInfo {
    val allSelections: MutableStateFlow<List<TransferRouteSelection>> = MutableStateFlow(
        listOf(
            TransferRouteSelection.Instant,
            TransferRouteSelection.Regular,
        ),
    )

    val selected: MutableStateFlow<TransferRouteSelection> = MutableStateFlow(
        TransferRouteSelection.Regular,
    )
}
