package de.rki.coronawarnapp.srs.ui.vm

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.srs.core.model.TekPatch
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class TeksSharedViewModel(
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val teksFlow = MutableStateFlow(tekPatch)

    val teks: Flow<TekPatch> = teksFlow.filterNotNull()

    init {
        teksFlow
            .onEach { tekPatch = it }
            .catch {
                Timber.tag(TAG).e(it, "Failed to save tekPatch")
            }
            .launchIn(viewModelScope)
    }

    suspend fun setTekPatch(tekPatch: TekPatch) = teksFlow.emit(tekPatch)

    suspend fun osTeks(): List<TemporaryExposureKey> = teks.first().osKeys()

    private var tekPatch: TekPatch?
        get() = savedState[TEK_PATCH_KEY]
        set(value) {
            Timber.tag(TAG).v("Saving %s into savedStateHandle", value)
            savedState[TEK_PATCH_KEY] = value
        }

    companion object {
        private val TAG = tag<TeksSharedViewModel>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val TEK_PATCH_KEY = "srs_cached_keys"
    }
}
