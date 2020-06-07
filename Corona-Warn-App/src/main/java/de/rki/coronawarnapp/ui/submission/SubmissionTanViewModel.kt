package de.rki.coronawarnapp.ui.submission

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.util.TanHelper

class SubmissionTanViewModel : ViewModel() {

    companion object {
        private val TAG: String? = SubmissionTanViewModel::class.simpleName
    }

    val tan = MutableLiveData<String?>(null)

    val isValidTanFormat =
        Transformations.map(tan) {
            it != null &&
            it.length == TanConstants.MAX_LENGTH &&
            TanHelper.isChecksumValid(it) &&
            TanHelper.allCharactersValid(it)
        }

    val tanChecksumValid =
        Transformations.map(tan) {
            if (it !== null && it.trim().length == TanConstants.MAX_LENGTH) TanHelper.isChecksumValid(it)
            else true
        }

    val tanCharactersValid =
        Transformations.map(tan) {
            if (it != null) TanHelper.allCharactersValid(it)
            else true
        }

    fun storeTeletan() {
        val teletan = tan.value!!
        Log.d(TAG, "Storing teletan $teletan")
        SubmissionRepository.setTeletan(teletan)
    }
}
