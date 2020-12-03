package de.rki.coronawarnapp.util

sealed class NetworkRequestWrapper<out T, out U> {
    object RequestIdle : NetworkRequestWrapper<Nothing, Nothing>()
    object RequestStarted : NetworkRequestWrapper<Nothing, Nothing>()
    data class RequestSuccessful<T, U>(val data: T) : NetworkRequestWrapper<T, U>()
    data class RequestFailed<T, U>(val error: U) : NetworkRequestWrapper<T, U>()

    companion object {
        fun <T, U, W> NetworkRequestWrapper<T, U>?.withSuccess(without: W, block: (data: T) -> W): W {
            return if (this is RequestSuccessful) {
                block(this.data)
            } else {
                without
            }
        }

        fun <T, U> NetworkRequestWrapper<T, U>?.withSuccess(block: (data: T) -> Unit) {
            if (this is RequestSuccessful) {
                block(this.data)
            }
        }

        fun <T, U> NetworkRequestWrapper<T, U>?.withFailure(block: (error: U) -> Unit) {
            if (this is RequestFailed) {
                block(this.error)
            }
        }
    }
}
