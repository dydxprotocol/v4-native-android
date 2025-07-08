package exchange.dydx.trading.feature.shared.analytics

//
// Events defined in the v4-web repo. Ideally, we should keep this in-sync with v4-web
//
enum class AnalyticsEvent(val rawValue: String) {
    // App
    APP_START("AppStart"),
    NETWORK_STATUS("NetworkStatus"),

    // Navigation
    NAVIGATE_PAGE("NavigatePage"),
    NAVIGATE_DIALOG("NavigateDialog"),
    NAVIGATE_DIALOG_CLOSE("NavigateDialogClose"),
    NAVIGATE_EXTERNAL("NavigateExternal"),

    // Wallet
    CONNECT_WALLET("ConnectWallet"),
    DISCONNECT_WALLET("DisconnectWallet"),

    // Onboarding
    ONBOARDING_STEP_CHANGED("OnboardingStepChanged"),
    ONBOARDING_ACCOUNT_DERIVED("OnboardingAccountDerived"),
    ONBOARDING_WALLET_IS_NON_DETERMINISTIC("OnboardingWalletIsNonDeterministic"),

    // Transfers
    TRANSFER_FAUCET("TransferFaucet"),
    TRANSFER_FAUCET_CONFIRMED("TransferFaucetConfirmed"),
    TRANSFER_DEPOSIT("TransferDeposit"),
    TRANSFER_WITHDRAW("TransferWithdraw"),

    // Trading
    TRADE_ORDER_TYPE_SELECTED("TradeOrderTypeSelected"),
    TRADE_PLACE_ORDER("TradePlaceOrder"),
    TRADE_PLACE_ORDER_CONFIRMED("TradePlaceOrderConfirmed"),
    TRADE_CANCEL_ORDER("TradeCancelOrder"),
    TRADE_CANCEL_ORDER_CONFIRMED("TradeCancelOrderConfirmed"),

    // Notification
    NOTIFICATION_ACTION("NotificationAction"),

    // Vault
    VaultFormPreviewStep("VaultFormPreviewStep"),
    AttemptVaultOperation("AttemptVaultOperation"),
    SuccessfulVaultOperation("SuccessfulVaultOperation"),
    VaultOperationProtocolError("VaultOperationProtocolError"),
}

//
// User properties to be sent to the analytics service
//
enum class UserProperty(val rawValue: String) {
    walletAddress("walletAddress"),
    walletType("walletType"),
    network("network"),
    selectedLocale("selectedLocale"),
    dydxAddress("dydxAddress"),
    subaccountNumber("subaccountNumber")
}
