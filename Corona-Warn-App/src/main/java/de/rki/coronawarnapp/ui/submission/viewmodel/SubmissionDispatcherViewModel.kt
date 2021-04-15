package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionDispatcherViewModel @AssistedInject constructor() : CWAViewModel() {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    private val profileCardIdData = MutableLiveData<Int>()
    private val created = false // TODO get from settings
    val profileCardId: LiveData<Int> = profileCardIdData

    init {
        profileCardIdData.value = if (created) {
            R.layout.submission_open_rat_profile_card
        } else {
            R.layout.submission_create_rat_profile_card
        }
    }

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
    }

    fun onTanPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToTAN)
    }

    fun onTeleTanPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToContact)
    }

    fun onQRCodePressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToConsent)
    }

    fun onClickProfileCard() {
        val event = if (created) {
            SubmissionNavigationEvents.NavigateToOpenProfile
        } else {
            SubmissionNavigationEvents.NavigateToCreateProfile
        }
        routeToScreen.postValue(event)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDispatcherViewModel>
}
