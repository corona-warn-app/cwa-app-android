package de.rki.coronawarnapp.util.viewmodel

interface SimpleCWAViewModelFactory<T : CWAViewModel> : CWAViewModelFactory<T> {
    fun create(): T
}
