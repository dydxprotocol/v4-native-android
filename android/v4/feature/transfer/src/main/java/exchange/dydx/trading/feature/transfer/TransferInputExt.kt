package exchange.dydx.trading.feature.transfer

import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.trading.common.featureflags.DydxFeatureFlags
import exchange.dydx.trading.feature.shared.TransferTokenDetails
import java.math.BigInteger
import kotlin.math.pow

fun TransferInput.tokenAddress(featureFlags: DydxFeatureFlags): String? {
    return token
}

fun TransferInput.tokenDecimals(transferTokenDetails: TransferTokenDetails): Int? {
    val goFastToken = transferTokenDetails.infos.value
        .firstOrNull { it.tokenAddress == token && it.chainId == chain }

    if (goFastToken != null) {
        return goFastToken.decimals
    }

    val tokenKey = token
    val decimals = tokenKey?.let {
        resources?.tokenResources?.get(it)?.decimals?.toInt()
    }

    return decimals
}

fun TransferInput.tokenSize(transferTokenDetails: TransferTokenDetails): BigInteger? {
    val size = size?.size?.toDouble()
    val decimals = tokenDecimals(transferTokenDetails)
    if (size != null && decimals != null) {
        val intSize = size * 10.0.pow(decimals)
        return intSize.toBigDecimal().toBigInteger()
    }

    return null
}
