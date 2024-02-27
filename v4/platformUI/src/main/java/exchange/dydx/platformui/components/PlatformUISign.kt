package exchange.dydx.platformui.components

enum class PlatformUISign {
    Plus, Minus, None;

    companion object {
        fun from(value: Double?): PlatformUISign {
            return when {
                value ?: 0.0 > 0 -> Plus
                value ?: 0.0 < 0 -> Minus
                else -> None
            }
        }
    }
}
