package exchange.dydx.trading.feature.transfer.steps

import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.trading.feature.shared.DydxScreenResult
import exchange.dydx.utilities.utils.AsyncEvent
import exchange.dydx.utilities.utils.AsyncStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter

class DydxScreenStep(
    val address: String,
    val abacusStateManager: AbacusStateManagerProtocol,
) : AsyncStep<Unit, Restriction> {

    private val eventFlow: MutableStateFlow<AsyncEvent<Unit, Restriction>> = MutableStateFlow(AsyncEvent.Progress(Unit))

    override fun run(): Flow<AsyncEvent<Unit, Restriction>> {
        abacusStateManager.screen(address = address) {
            eventFlow.value = AsyncEvent.Result(
                result = it,
                error = null,
            )
        }

        return eventFlow
    }
}

class DydxTransferScreenStep(
    val originationAddress: String,
    val destinationAddress: String,
    val transferInput: TransferInput,
    val abacusStateManager: AbacusStateManagerProtocol,
) : AsyncStep<Unit, DydxScreenResult> {

    override fun run(): Flow<AsyncEvent<Unit, DydxScreenResult>> {
        return combine(
            DydxScreenStep(
                address = originationAddress,
                abacusStateManager = abacusStateManager,
            ).run().filter { it.isResult },
            DydxScreenStep(
                address = destinationAddress,
                abacusStateManager = abacusStateManager,
            ).run().filter { it.isResult },
        ) { originationScreen, destinationScreen ->
            if (originationScreen is AsyncEvent.Result && originationScreen.result == Restriction.USER_RESTRICTED) {
                return@combine AsyncEvent.Result(
                    result = DydxScreenResult.SourceRestriction,
                    error = null,
                )
            }
            if (destinationScreen is AsyncEvent.Result && destinationScreen.result == Restriction.USER_RESTRICTED) {
                return@combine AsyncEvent.Result(
                    result = DydxScreenResult.DestinationRestriction,
                    error = null,
                )
            }

            listOf(originationScreen, destinationScreen).forEach {
                if (it is AsyncEvent.Result) {
                    when (it.result) {
                        Restriction.GEO_RESTRICTED -> {
                            return@combine AsyncEvent.Result(
                                result = DydxScreenResult.GeoRestriction,
                                error = null,
                            )
                        }

                        Restriction.USER_RESTRICTION_UNKNOWN -> {
                            return@combine AsyncEvent.Result(
                                result = DydxScreenResult.UnknownRestriction,
                                error = null,
                            )
                        }

                        else -> {
                            // Do nothing
                        }
                    }
                }
            }

            AsyncEvent.Result(
                result = DydxScreenResult.NoRestriction,
                error = null,
            )
        }
    }
}
