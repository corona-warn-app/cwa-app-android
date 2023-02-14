package de.rki.coronawarnapp.rootdetection.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.rootdetection.core.RootDetectionCheck
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RootDetectionDialogViewModel @Inject constructor(
    private val rootDetectionCheck: RootDetectionCheck
) : CWAViewModel() {

    fun onSuppressCheckedChanged(isChecked: Boolean) {
        Timber.tag(TAG).d("onSuppressCheckedChanged(isChecked=%s)", isChecked)
        rootDetectionCheck.suppressRootInfoForCurrentVersion(suppress = isChecked)
    }

    companion object {
        private val TAG = tag<RootDetectionDialogViewModel>()
    }
}
