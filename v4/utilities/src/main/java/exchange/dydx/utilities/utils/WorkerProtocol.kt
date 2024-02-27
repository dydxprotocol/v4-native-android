package exchange.dydx.utilities.utils

import kotlinx.coroutines.CoroutineScope

interface WorkerProtocol {
    fun start()
    fun stop()
    var isStarted: Boolean
    val scope: CoroutineScope
}
