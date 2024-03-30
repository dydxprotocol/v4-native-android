package exchange.dydx.trading.common.navigation

object OnboardingRoutes {
    const val desktop_scan = "onboard/scan"
    const val debug_scan = "onboard/qrcode"
    const val wallet_list = "onboard/wallets"
    const val connect = "onboard/connect"
    const val welcome = "onboard"
    const val tos = "onboard/tos"
}

object PortfolioRoutes {
    const val main = "portfolio"
    const val order_details = "orders"
}

object ProfileRoutes {
    const val main = "my-profile"
    const val settings = "settings"
    const val language = "settings/language"
    const val theme = "settings/theme"
    const val env = "settings/env"
    const val status = "settings/status"
    const val update = "update"
    const val features = "features"
    const val debug = "settings/debug"
    const val color = "settings/direction_color_preference"
    const val wallets = "wallets"
    const val key_export = "my-profile/keyexport"
    const val history = "portfolio/history"
    const val fees_structure = "profile/fees"
    const val help = "help"
    const val rewards = "rewards"
    const val debug_enable = "action/debug/enable"
    const val report_issue = "settings/report_issue"
}

object NewsAlertsRoutes {
    const val main = "alerts_main"
}

object MarketRoutes {
    const val marketList = "markets"
    const val marketInfo = "market"
    const val marketSearch = "market/search"
}

object TradeRoutes {
    const val status = "trade/status"
    const val close_position = "trade/close"
}

object TransferRoutes {
    const val transfer = "transfer"
    const val transfer_search = "transfer/search"
    const val transfer_status = "transfer/status"
}
