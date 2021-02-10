package de.rki.coronawarnapp.nearby.modules.tekhistory

sealed class TEKResult<out T> {
    data class Success<out T : Any>(val data: T) : TEKResult<T>()
    data class Error(val exception: Exception?) : TEKResult<Nothing>()
}
