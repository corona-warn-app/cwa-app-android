package de.rki.coronawarnapp.test.menu.ui

import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.test.api.ui.TestForAPIFragment
import de.rki.coronawarnapp.test.risklevel.ui.TestRiskLevelCalculationFragment
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TestMenuFragmentViewModel @AssistedInject constructor() : CWAViewModel() {

    val testMenuData by lazy {
        listOf(
            TestForAPIFragment.MENU_ITEM,
            TestRiskLevelCalculationFragment.MENU_ITEM
        ).let { MutableLiveData(it) }
    }
    val showTestScreenEvent = SingleLiveEvent<TestMenuItem>()

    fun showTestScreen(it: TestMenuItem) {
        showTestScreenEvent.postValue(it)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<TestMenuFragmentViewModel>
}
