package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.ThreadingProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class AbacusThreadingImp @Inject constructor() : ThreadingProtocol {
    private val mainScope = MainScope()

    // Abacus runs lots of computations, but needs to be run without parallelism
    private val abacusScope = MainScope() + Dispatchers.Default.limitedParallelism(1)
    private val networkScope = MainScope() + Dispatchers.IO
    override fun async(type: ThreadingType, block: () -> Unit) {
        when (type) {
            ThreadingType.main ->
                mainScope
                    .launch {
                        block()
                    }

            ThreadingType.abacus ->
                abacusScope
                    .launch {
                        block()
                    }

            ThreadingType.network ->
                networkScope
                    .launch {
                        block()
                    }
        }
    }
}
