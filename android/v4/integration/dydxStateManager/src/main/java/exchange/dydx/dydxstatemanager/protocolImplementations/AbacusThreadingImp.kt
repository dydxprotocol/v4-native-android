package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.ThreadingProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.ThreadingType.abacus
import exchange.dydx.abacus.protocols.ThreadingType.main
import exchange.dydx.abacus.protocols.ThreadingType.network
import exchange.dydx.trading.common.di.CoroutineDispatchers
import exchange.dydx.trading.common.di.CoroutineScopes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class AbacusThreadingImp @Inject constructor(
    @CoroutineScopes.App private val appScope: CoroutineScope,
    @CoroutineDispatchers.IO private val ioDispatcher: CoroutineDispatcher,
    @CoroutineDispatchers.Default private val defaultDispatcher: CoroutineDispatcher,
) : ThreadingProtocol {

    private val mainScope = appScope

    // Abacus runs lots of computations, but needs to be run without parallelism
    private val abacusScope = appScope + defaultDispatcher.limitedParallelism(1)
    private val networkScope = appScope + ioDispatcher

    override fun async(type: ThreadingType, block: () -> Unit) {
        when (type) {
            main ->
                mainScope
                    .launch {
                        block()
                    }

            abacus ->
                abacusScope
                    .launch {
                        block()
                    }

            network ->
                networkScope
                    .launch {
                        block()
                    }
        }
    }
}
