package exchange.dydx.trading.feature.receipt

sealed class TradeReceiptType {
    data object Open : TradeReceiptType()
    data object Close : TradeReceiptType()
}

sealed class ReceiptType {
    data class Trade(val tradeReceiptType: TradeReceiptType) : ReceiptType()
    data object Transfer : ReceiptType()
}
