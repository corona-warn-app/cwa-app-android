package de.rki.coronawarnapp.contactdiary.ui.location

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ContactDiaryAddLocationViewModel @AssistedInject constructor(
    @AppScope private val appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    @Assisted private val addedAt: String?,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        launchErrorHandler = CoroutineExceptionHandler { _, ex ->
            shouldClose.postValue(Unit)
            ex.report(ExceptionCategory.INTERNAL, TAG)
        }
    }

    val shouldClose = SingleLiveEvent<Unit>()

    private val locationName = MutableStateFlow("")

    val isValid = locationName
        .map { it.isNotEmpty() }
        .asLiveData()

    fun locationChanged(value: String) {
        locationName.value = value
    }

    fun addLocation(phoneNumber: String, emailAddress: String) = launch(scope = appScope) {
        val location = contactDiaryRepository.addLocation(
            DefaultContactDiaryLocation(
                locationName = locationName.value,
                phoneNumber = phoneNumber,
                emailAddress = emailAddress
            )
        )

        addedAt?.let {
            contactDiaryRepository.addLocationVisit(
                DefaultContactDiaryLocationVisit(
                    date = LocalDate.parse(it),
                    contactDiaryLocation = location
                )
            )
        }
        shouldClose.postValue(Unit)
    }

    fun updateLocation(location: ContactDiaryLocationEntity, phoneNumber: String, emailAddress: String) =
        launch(scope = appScope) {
            contactDiaryRepository.updateLocation(
                DefaultContactDiaryLocation(
                    location.locationId,
                    locationName = locationName.value,
                    phoneNumber = phoneNumber,
                    emailAddress = emailAddress
                )
            )
            shouldClose.postValue(Unit)
        }

    fun deleteLocation(location: ContactDiaryLocationEntity) = launch(scope = appScope) {
        contactDiaryRepository.locationVisits.firstOrNull()?.forEach {
            if (it.contactDiaryLocation.locationId == location.locationId)
                contactDiaryRepository.deleteLocationVisit(it)
        }
        contactDiaryRepository.deleteLocation(location)
        shouldClose.postValue(Unit)
    }

    fun closePressed() {
        shouldClose.postValue(Unit)
    }

    companion object {
        private val TAG = tag<ContactDiaryAddLocationViewModel>()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ContactDiaryAddLocationViewModel> {
        fun create(addedAt: String?): ContactDiaryAddLocationViewModel
    }
}
