package exchange.dydx.trading.common

/**
 * A generic class that holds a value or error.
 * @param <T>
 */
sealed class AsyncResult<out R> {
    abstract val data: R?

    /**
     * Check for timeout or other high level validation logic.
     */
    open fun check(): AsyncResult<R>? { return this }

    object Waiting : AsyncResult<Nothing>() {
        override val data: Nothing? = null
    }

    data class WaitingTimeout(
        val timeoutDurationMillis: Int = 0,
        val currentTimeMillis: (() -> Long) = { System.currentTimeMillis() },
    ) : AsyncResult<Nothing>() {
        val timeoutTimeMillis = currentTimeMillis() + timeoutDurationMillis
        override val data: Nothing? = null
        override fun check(): WaitingTimeout? {
            if (currentTimeMillis() > timeoutTimeMillis) {
                return null
            }
            return this
        }
    }

    data class Success<out T>(override val data: T) : AsyncResult<T>()

    /**
     * Error indicates a significant / non-recoverable failure of processing.
     *
     * Data will be provided if available but may be stale or incomplete.
     */
    data class Failure<out T>(val exception: Exception = RuntimeException(), override val data: T? = null) : AsyncResult<T>()

    override fun toString(): String {
        return when (this) {
            Waiting -> "Waiting"
            is WaitingTimeout -> "WaitingUntil[timeout=$timeoutTimeMillis]"
            is Success<*> -> "Success[data=$data]"
            is Failure -> "Failure[exception=$exception data=$data]"
        }
    }
}
