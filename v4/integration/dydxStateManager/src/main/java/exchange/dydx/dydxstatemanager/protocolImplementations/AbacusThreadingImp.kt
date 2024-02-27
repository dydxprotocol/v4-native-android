package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.ThreadingProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.plus

class AbacusThreadingImp : ThreadingProtocol {
    private val mainScope = CoroutineScope(Dispatchers.Main) // + Job())
    private val abacusScope = CoroutineScope(newSingleThreadContext("AbacusScope"))
    private val networkScope = CoroutineScope(Dispatchers.IO)
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
