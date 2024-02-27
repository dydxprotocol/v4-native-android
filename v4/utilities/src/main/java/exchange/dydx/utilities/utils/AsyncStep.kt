package exchange.dydx.utilities.utils

import kotlinx.coroutines.flow.Flow

// Sealed class to represent async events
sealed class AsyncEvent<out ProgressType, out ResultType> {
    data class Progress<ProgressType>(val progress: ProgressType) : AsyncEvent<ProgressType, Nothing>()
    data class Result<ResultType>(val result: ResultType?, val error: Throwable?) : AsyncEvent<Nothing, ResultType>()

    val isProgress: Boolean
        get() = this is Progress

    val isResult: Boolean
        get() = this is Result
}

// Interface representing an asynchronous step
interface AsyncStep<ProgressType, ResultType> {
    fun run(): Flow<AsyncEvent<ProgressType, ResultType>>

    val invalidInputEvent: AsyncEvent.Result<ResultType>
        get() = errorEvent("Invalid input")

    fun errorEvent(error: String): AsyncEvent.Result<ResultType> = AsyncEvent.Result<ResultType>(
        result = null,
        error = Throwable(error),
    )
}
