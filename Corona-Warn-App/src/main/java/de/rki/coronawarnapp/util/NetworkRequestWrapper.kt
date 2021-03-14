package de.rki.coronawarnapp.util

sealed class NetworkRequestWrapper<out T : Any, out U : Any> {
    object RequestIdle : NetworkRequestWrapper<Nothing, Nothing>()
    object RequestStarted : NetworkRequestWrapper<Nothing, Nothing>()
    data class RequestSuccessful<T : Any, U : Any>(val data: T) : NetworkRequestWrapper<T, U>()
    data class RequestFailed<T : Any, U : Throwable>(val error: U) : NetworkRequestWrapper<T, U>()

    companion object {
        fun <T : Any, U : Any, W> NetworkRequestWrapper<T, U>?.withSuccess(without: W, block: (data: T) -> W): W {
            return if (this is RequestSuccessful) {
                block(this.data)
            } else {
                without
            }
        }

        fun <T : Any, U : Any> NetworkRequestWrapper<T, U>?.withSuccess(block: (data: T) -> Unit) {
            if (this is RequestSuccessful) {
                block(this.data)
            }
        }

        fun <T : Any, U : Any> NetworkRequestWrapper<T, U>?.withFailure(block: (error: U) -> Unit) {
            if (this is RequestFailed) {
                block(this.error)
            }
        }
    }
}
