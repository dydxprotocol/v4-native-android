package exchange.dydx.dydxCartera

import exchange.dydx.cartera.entities.Wallet

fun Wallet.imageUrl(folder: String?): String? {
    val imageName = userFields?.get("imageName")
    return if (imageName != null && folder != null) {
        "$folder$imageName"
    } else {
        null
    }
}
