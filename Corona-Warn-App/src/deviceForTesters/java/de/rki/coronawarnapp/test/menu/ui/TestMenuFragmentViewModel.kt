package de.rki.coronawarnapp.test.menu.ui

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.miscinfo.MiscInfoFragment
import de.rki.coronawarnapp.test.appconfig.ui.AppConfigTestFragment
import de.rki.coronawarnapp.test.ccl.CCLTestFragment
import de.rki.coronawarnapp.test.contactdiary.ui.ContactDiaryTestFragment
import de.rki.coronawarnapp.test.crash.ui.SettingsCrashReportFragment
import de.rki.coronawarnapp.test.datadonation.ui.DataDonationTestFragment
import de.rki.coronawarnapp.test.dccticketing.DccTicketingTestFragment
import de.rki.coronawarnapp.test.debugoptions.ui.DebugOptionsFragment
import de.rki.coronawarnapp.test.deltaonboarding.ui.DeltaOnboardingFragment
import de.rki.coronawarnapp.test.dsc.ui.DscTestFragment
import de.rki.coronawarnapp.test.hometestcards.ui.HomeTestCardsFragment
import de.rki.coronawarnapp.test.keydownload.ui.KeyDownloadTestFragment
import de.rki.coronawarnapp.test.playground.ui.PlaygroundFragment
import de.rki.coronawarnapp.test.presencetracing.ui.PresenceTracingTestFragment
import de.rki.coronawarnapp.test.qrcode.ui.QrCodeTestFragment
import de.rki.coronawarnapp.test.risklevel.ui.TestRiskLevelCalculationFragment
import de.rki.coronawarnapp.test.submission.ui.SubmissionTestFragment
import de.rki.coronawarnapp.test.tasks.ui.TestTaskControllerFragment
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class TestMenuFragmentViewModel @AssistedInject constructor(
    personCertificatesProvider: PersonCertificatesProvider
) : CWAViewModel() {

    val personsCount = personCertificatesProvider.personCertificates.map { it.size }.asLiveData2()

    val testMenuData by lazy {
        listOf(
            CCLTestFragment.MENU_ITEM,
            DebugOptionsFragment.MENU_ITEM,
            SettingsCrashReportFragment.MENU_ITEM,
            AppConfigTestFragment.MENU_ITEM,
            TestRiskLevelCalculationFragment.MENU_ITEM,
            MiscInfoFragment.MENU_ITEM,
            KeyDownloadTestFragment.MENU_ITEM,
            TestTaskControllerFragment.MENU_ITEM,
            SubmissionTestFragment.MENU_ITEM,
            ContactDiaryTestFragment.MENU_ITEM,
            PlaygroundFragment.MENU_ITEM,
            DataDonationTestFragment.MENU_ITEM,
            DeltaOnboardingFragment.MENU_ITEM,
            PresenceTracingTestFragment.MENU_ITEM,
            HomeTestCardsFragment.MENU_ITEM,
            QrCodeTestFragment.MENU_ITEM,
            DscTestFragment.MENU_ITEM,
            DccTicketingTestFragment.MENU_ITEM,
        ).let { MutableLiveData(it) }
    }
    val showTestScreenEvent = SingleLiveEvent<TestMenuItem>()

    fun showTestScreen(it: TestMenuItem) {
        showTestScreenEvent.postValue(it)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TestMenuFragmentViewModel>
}
