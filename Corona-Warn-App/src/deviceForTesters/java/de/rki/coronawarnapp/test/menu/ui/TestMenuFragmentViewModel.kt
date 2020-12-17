package de.rki.coronawarnapp.test.menu.ui

import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.miscinfo.MiscInfoFragment
import de.rki.coronawarnapp.test.appconfig.ui.AppConfigTestFragment
import de.rki.coronawarnapp.test.contactdiary.ui.ContactDiaryTestFragment
import de.rki.coronawarnapp.test.crash.ui.SettingsCrashReportFragment
import de.rki.coronawarnapp.test.debugoptions.ui.DebugOptionsFragment
import de.rki.coronawarnapp.test.keydownload.ui.KeyDownloadTestFragment
import de.rki.coronawarnapp.test.risklevel.ui.TestRiskLevelCalculationFragment
import de.rki.coronawarnapp.test.submission.ui.SubmissionTestFragment
import de.rki.coronawarnapp.test.tasks.ui.TestTaskControllerFragment
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TestMenuFragmentViewModel @AssistedInject constructor() : CWAViewModel() {

    val testMenuData by lazy {
        listOf(
            DebugOptionsFragment.MENU_ITEM,
            AppConfigTestFragment.MENU_ITEM,
            TestRiskLevelCalculationFragment.MENU_ITEM,
            KeyDownloadTestFragment.MENU_ITEM,
            TestTaskControllerFragment.MENU_ITEM,
            SubmissionTestFragment.MENU_ITEM,
            SettingsCrashReportFragment.MENU_ITEM,
            MiscInfoFragment.MENU_ITEM,
            ContactDiaryTestFragment.MENU_ITEM
        ).let { MutableLiveData(it) }
    }
    val showTestScreenEvent = SingleLiveEvent<TestMenuItem>()

    fun showTestScreen(it: TestMenuItem) {
        showTestScreenEvent.postValue(it)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<TestMenuFragmentViewModel>
}
