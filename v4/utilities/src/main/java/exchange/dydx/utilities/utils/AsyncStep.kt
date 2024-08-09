package exchange.dydx.utilities.utils

// Interface representing an asynchronous step
interface AsyncStep<ResultType> {
    suspend fun run(): Result<ResultType>

    val invalidInputEvent: Result<ResultType>
        get() = errorEvent("Invalid input")

    fun errorEvent(error: String) = Result.failure<ResultType>(Throwable(error))
}
