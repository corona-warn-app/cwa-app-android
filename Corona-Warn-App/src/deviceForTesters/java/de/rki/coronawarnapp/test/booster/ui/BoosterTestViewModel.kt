package de.rki.coronawarnapp.test.booster.ui

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.booster.BoosterNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class BoosterTestViewModel @AssistedInject constructor(
    private val boosterNotificationService: BoosterNotificationService,
    private val boosterRulesRepository: BoosterRulesRepository
) : CWAViewModel() {
    fun refreshBoosterRules() = launch {
        boosterRulesRepository.updateBoosterNotificationRules()
    }

    fun clearBoosterRules() = launch {
        boosterRulesRepository.clear()
    }

    fun runBoosterRules() = launch {
        boosterNotificationService.checkBoosterNotification()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<BoosterTestViewModel>

    val errorEvent = SingleLiveEvent<Unit>()
}
