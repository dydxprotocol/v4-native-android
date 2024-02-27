package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.TimerProtocol
import java.util.Timer
import java.util.TimerTask

class AbacusTimerImp : TimerProtocol {
    override fun schedule(
        delay: Double,
        repeat: Double?,
        block: () -> Boolean,
    ): LocalTimerProtocol {
        return LocalTimer(delay, repeat, block)
    }
}

class LocalTimer(
    private val delay: Double,
    private val timeInterval: Double?,
    private val block: () -> Boolean,
) : LocalTimerProtocol {
    private var timer: Timer? = null

    init {
        val daleyInMilliseconds = delay * 1000
        val repeatInMilliseconds = timeInterval?.let { it * 1000 }

        timer = Timer()
        timer?.schedule(
            object : TimerTask() {
                override fun run() {
                    val shouldContinue = block()
                    if (shouldContinue && repeatInMilliseconds != null) {
                        val innerTimer = Timer()
                        innerTimer.scheduleAtFixedRate(
                            object : TimerTask() {
                                override fun run() {
                                    val innerShouldContinue = block()
                                    if (!innerShouldContinue) {
                                        innerTimer.cancel()
                                    }
                                }
                            },
                            repeatInMilliseconds.toLong(),
                            repeatInMilliseconds.toLong(),
                        )
                    } else {
                        cancel()
                    }
                }
            },
            daleyInMilliseconds.toLong(),
        )
    }

    override fun cancel() {
        timer?.cancel()
        timer?.purge()
        timer = null
    }
}
