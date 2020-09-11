package de.rki.coronawarnapp.util.viewmodel

interface SimpleVDCFactory<T : VDC> : VDCFactory<T> {
    fun create(): T
}
