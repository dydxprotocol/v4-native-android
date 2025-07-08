package exchange.dydx.trading.feature.transfer.steps

import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.feature.shared.DydxScreenResult
import exchange.dydx.utilities.utils.AsyncStep
import exchange.dydx.utilities.utils.runWithLogs
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DydxScreenStep(
    val address: String,
    val abacusStateManager: AbacusStateManagerProtocol,
) : AsyncStep<Restriction> {

    override suspend fun run(): Result<Restriction> = suspendCoroutine { continuation ->
        abacusStateManager.screen(address = address) {
            continuation.resume(Result.success(it))
        }
    }
}

class DydxTransferScreenStep(
    val originationAddress: String,
    val destinationAddress: String,
    val transferInput: TransferInput,
    val abacusStateManager: AbacusStateManagerProtocol,
) : AsyncStep<DydxScreenResult> {

    override suspend fun run(): Result<DydxScreenResult> =
        coroutineScope {
            val originationScreenAsync = async {
                DydxScreenStep(
                    address = originationAddress,
                    abacusStateManager = abacusStateManager,
                ).runWithLogs()
            }

            val destinationScreenAsync = async {
                DydxScreenStep(
                    address = destinationAddress,
                    abacusStateManager = abacusStateManager,
                ).runWithLogs()
            }

            val originationScreen = originationScreenAsync.await()
            val destinationScreen = destinationScreenAsync.await()

            if (originationScreen.isSuccess && originationScreen.getOrThrow() == Restriction.USER_RESTRICTED) {
                return@coroutineScope Result.success(
                    DydxScreenResult.SourceRestriction,
                )
            }
            if (destinationScreen.isSuccess && destinationScreen.getOrThrow() == Restriction.USER_RESTRICTED) {
                return@coroutineScope Result.success(
                    DydxScreenResult.DestinationRestriction,
                )
            }

            listOf(originationScreen, destinationScreen).forEach {
                if (it.isSuccess) {
                    when (it.getOrThrow()) {
                        Restriction.GEO_RESTRICTED -> {
                            return@coroutineScope Result.success(
                                DydxScreenResult.GeoRestriction,
                            )
                        }

                        Restriction.USER_RESTRICTION_UNKNOWN -> {
                            return@coroutineScope Result.success(
                                DydxScreenResult.UnknownRestriction,
                            )
                        }

                        else -> {
                            // Do nothing
                        }
                    }
                }
            }

            return@coroutineScope Result.success(
                DydxScreenResult.NoRestriction,
            )
        }
}
