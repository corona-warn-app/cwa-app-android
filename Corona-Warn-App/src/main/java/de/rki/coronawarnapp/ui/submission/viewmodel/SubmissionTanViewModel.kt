package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.TanConstants
import de.rki.coronawarnapp.util.TanHelper
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionTanViewModel @AssistedInject constructor() : CWAViewModel() {

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
        SubmissionRepository.setTeletan(teletan)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTanViewModel>
}
