package de.rki.coronawarnapp.ui.submission

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.storage.SubmissionRepository

class SubmissionTanViewModel : ViewModel() {

    companion object {
        private val TAG: String? = SubmissionTanViewModel::class.simpleName

        private const val TAN_LENGTH = 7

        // TODO switch these back after testing
        // private val EXCLUDED_TAN_CHARS = listOf('0', 'O', 'I', '1')
        // private val VALID_TAN_CHARS = ('A'..'Z').plus('0'..'9').minus(EXCLUDED_TAN_CHARS)
        private val VALID_TAN_CHARS = ('A'..'Z').plus('0'..'9')
    }

    val tan = MutableLiveData<String?>(null)

    val isValidTanFormat =
        Transformations.map(tan) {
            it != null && it.length == TAN_LENGTH && it.all { c -> VALID_TAN_CHARS.contains(c) }
        }

    fun storeTeletan() {
        val teletan = tan.value!!
        Log.d(TAG, "Storing teletan $teletan")
        SubmissionRepository.setTeletan(teletan)
    }
}
