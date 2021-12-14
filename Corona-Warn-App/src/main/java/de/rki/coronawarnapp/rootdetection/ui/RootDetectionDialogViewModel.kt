package de.rki.coronawarnapp.rootdetection.ui

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.rootdetection.core.RootDetectionCheck
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class RootDetectionDialogViewModel @AssistedInject constructor(
    private val rootDetectionCheck: RootDetectionCheck
) : CWAViewModel() {

    fun onSuppressCheckedChanged(isChecked: Boolean) {
        Timber.tag(TAG).d("onSuppressCheckedChanged(isChecked=%s)", isChecked)
        rootDetectionCheck.suppressRootInfoForCurrentVersion(suppress = isChecked)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RootDetectionDialogViewModel>

    companion object {
        private val TAG = tag<RootDetectionDialogViewModel>()
    }
}
