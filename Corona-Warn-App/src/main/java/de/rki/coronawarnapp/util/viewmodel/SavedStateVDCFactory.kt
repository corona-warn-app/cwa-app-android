package de.rki.coronawarnapp.util.viewmodel

import androidx.lifecycle.SavedStateHandle

interface SavedStateVDCFactory<T : VDC> : VDCFactory<T> {
    fun create(handle: SavedStateHandle): T
}
