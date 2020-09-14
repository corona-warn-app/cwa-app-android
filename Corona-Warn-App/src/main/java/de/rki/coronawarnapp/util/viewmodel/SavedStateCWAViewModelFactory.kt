package de.rki.coronawarnapp.util.viewmodel

import androidx.lifecycle.SavedStateHandle

interface SavedStateCWAViewModelFactory<T : CWAViewModel> : CWAViewModelFactory<T> {
    fun create(handle: SavedStateHandle): T
}
