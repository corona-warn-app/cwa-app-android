package de.rki.coronawarnapp.release

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class NewReleaseInfoViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val settings: CWASettings
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<NewReleaseInfoNavigationEvents> = SingleLiveEvent()

    val title = R.string.release_info_version_title.toResolvingString(BuildConfig.VERSION_NAME)

    fun onNextButtonClick() {
        settings.lastChangelogVersion.update { BuildConfigWrap.VERSION_CODE }
        routeToScreen.postValue(NewReleaseInfoNavigationEvents.CloseScreen)
    }

    fun getItems(titles: Array<String>, bodies: Array<String>): List<NewReleaseInfoItem> {
        if (titles.size != bodies.size) {
            Timber.e("R.array.new_release_title and R.array.new_release_body must have the same size!")
        }
        val items = mutableListOf<NewReleaseInfoItem>()
        titles.indices.forEach {
            if (it <= bodies.lastIndex) {
                items.add(NewReleaseInfoItem(titles[it], bodies[it]))
            }
        }
        return items
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<NewReleaseInfoViewModel>
}
