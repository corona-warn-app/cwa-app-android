package de.rki.coronawarnapp.ui.main.overview

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainOverviewViewModel @Inject constructor(
    appConfigProvider: AppConfigProvider
) : CWAViewModel() {

    val maxEncounterAgeInDays = appConfigProvider.currentConfig.map { it.maxEncounterAgeInDays }.asLiveData2()
}
