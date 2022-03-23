package de.rki.coronawarnapp.familytest.ui.selection

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode.CategoryType
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.coroutine.modifyCategoryType
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first

class TestRegistrationSelectionViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    @Assisted private val coronaTestQRCode: CoronaTestQRCode,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<TestRegistrationSelectionNavigationEvents>()

    fun onNavigateToPerson() = launch {
        val currentCoronaTest = submissionRepository.testForType(coronaTestQRCode.type).first()
        val updatedCoronaTestQRCode = coronaTestQRCode.modifyCategoryType(CategoryType.OWN)
        val navigationEvent = if (currentCoronaTest != null) {
            TestRegistrationSelectionNavigationEvents.NavigateToDeletionWarning(updatedCoronaTestQRCode)
        } else {
            TestRegistrationSelectionNavigationEvents.NavigateToPerson(updatedCoronaTestQRCode)
        }
        routeToScreen.postValue(navigationEvent)
    }

    fun onNavigateToFamily() {
        routeToScreen.postValue(
            TestRegistrationSelectionNavigationEvents.NavigateToFamily(
                coronaTestQRCode.modifyCategoryType(
                    CategoryType.FAMILY
                )
            )
        )
    }

    fun onNavigateBack() {
        routeToScreen.postValue(TestRegistrationSelectionNavigationEvents.NavigateBack)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TestRegistrationSelectionViewModel> {
        fun create(
            coronaTestQRCode: CoronaTestQRCode
        ): TestRegistrationSelectionViewModel
    }
}
