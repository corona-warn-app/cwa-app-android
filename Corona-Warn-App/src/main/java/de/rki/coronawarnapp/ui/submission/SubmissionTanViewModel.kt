package de.rki.coronawarnapp.ui.submission

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.util.TanHelper
import timber.log.Timber

class SubmissionTanViewModel @ViewModelInject constructor(
    val submissionRepository: SubmissionRepository
) : ViewModel() {

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

    fun storeTeletan() {
        val teletan = tan.value!!
        Timber.d("Storing teletan $teletan")
        submissionRepository.setTeletan(teletan)
    }
}
