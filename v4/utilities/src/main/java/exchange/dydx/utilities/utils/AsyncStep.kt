package exchange.dydx.utilities.utils

import android.util.Log

// Interface representing an asynchronous step
interface AsyncStep<ResultType> {
    suspend fun run(): Result<ResultType>

    val invalidInputEvent: Result<ResultType>
        get() = errorEvent("Invalid input")

    fun errorEvent(error: String) = Result.failure<ResultType>(Throwable(error))
}

suspend inline fun <T, reified A : AsyncStep<T>> A.runWithLogs(): Result<T> {
    val name = A::class.java.simpleName
    Log.d("AsyncStep", "Starting $name")
    return run().also { Log.d("AsyncStep", "Got result for $name: $it") }
}
