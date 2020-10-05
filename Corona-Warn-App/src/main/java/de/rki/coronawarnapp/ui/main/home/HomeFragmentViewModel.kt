package de.rki.coronawarnapp.ui.main.home

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.launch

class HomeFragmentViewModel @AssistedInject constructor(
    private val context: Context,
    private val errorResetTool: EncryptionErrorResetTool
) : CWAViewModel() {

    val showTracingExplanationDialog = SingleLiveEvent<Long>()
    val showErrorResetDialog = SingleLiveEvent<Boolean>()

    init {
        if (!LocalData.tracingExplanationDialogWasShown()) {
            viewModelScope.launch {
                showTracingExplanationDialog.postValue(TimeVariables.getActiveTracingDaysInRetentionPeriod())
            }
        }
        if (errorResetTool.isResetNoticeToBeShown) {
            showErrorResetDialog.postValue(true)
        }
    }

    fun errorResetDialogDismissed() {
        errorResetTool.isResetNoticeToBeShown = false
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<HomeFragmentViewModel>
}
