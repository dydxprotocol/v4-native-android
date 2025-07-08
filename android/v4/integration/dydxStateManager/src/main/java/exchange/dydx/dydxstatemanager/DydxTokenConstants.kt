package exchange.dydx.dydxstatemanager

val AbacusStateManagerProtocol.usdcTokenKey: String
    get() = "usdc"

val AbacusStateManagerProtocol.nativeTokenKey: String
    get() = "chain"

val AbacusStateManagerProtocol.usdcTokenName: String
    get() = environment?.tokens?.toMap()?.get("usdc")?.name ?: "USDC"

val AbacusStateManagerProtocol.usdcTokenLogoUrl: String?
    get() = environment?.tokens?.toMap()?.get(usdcTokenKey)?.imageUrl

val AbacusStateManagerProtocol.nativeTokenName: String
    get() = environment?.tokens?.toMap()?.get(nativeTokenKey)?.name ?: "DYDX"

val AbacusStateManagerProtocol.nativeTokenLogoUrl: String?
    get() = environment?.tokens?.toMap()?.get(nativeTokenKey)?.imageUrl

val AbacusStateManagerProtocol.usdcTokenDecimal: Int
    get() = environment?.tokens?.toMap()?.get(usdcTokenKey)?.decimals ?: 2

val AbacusStateManagerProtocol.nativeTokenDecimal: Int
    get() = environment?.tokens?.toMap()?.get(nativeTokenKey)?.decimals ?: 18

val AbacusStateManagerProtocol.usdcTokenDenom: String?
    get() = environment?.tokens?.toMap()?.get(usdcTokenKey)?.denom

val AbacusStateManagerProtocol.nativeTokenDenom: String?
    get() = environment?.tokens?.toMap()?.get(nativeTokenKey)?.denom
