package de.rki.coronawarnapp.ui.release

import android.content.Context
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.flowOf

class NewReleaseInfoFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @AppContext private val context: Context,
    var settings: CWASettings
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    val routeToScreen: SingleLiveEvent<NewReleaseInfoFragmentNavigationEvents> = SingleLiveEvent()

    fun onNextButtonClick() {
        routeToScreen.postValue(NewReleaseInfoFragmentNavigationEvents.NavigateToMainActivity)
    }

    val appVersion = flowOf(
        context.getString(R.string.release_info_version_title).format(BuildConfig.VERSION_NAME)
    ).asLiveData(context = dispatcherProvider.Default)

    fun userHasReadReleaseNotes() {
        settings.lastChangelogVersion.update { BuildConfigWrap.VERSION_CODE }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<NewReleaseInfoFragmentViewModel>
}
