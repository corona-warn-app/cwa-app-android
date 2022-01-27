package de.rki.coronawarnapp.test.booster.ui

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.booster.BoosterNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import org.joda.time.Instant

class BoosterTestViewModel @AssistedInject constructor(
    private val boosterNotificationService: BoosterNotificationService,
    private val boosterRulesRepository: BoosterRulesRepository,
    private val covidCertificateSettings: CovidCertificateSettings,
    vaccinationRepository: VaccinationRepository,
) : CWAViewModel() {
    fun refreshBoosterRules() = launch {
        boosterRulesRepository.update()
    }

    val rules = boosterRulesRepository.rules.asLiveData2()
    val persons = vaccinationRepository.vaccinationInfos.asLiveData2()

    fun clearBoosterRules() = launch {
        boosterRulesRepository.clear()
    }

    fun runBoosterRules() = launch {
        covidCertificateSettings.lastDccBoosterCheck.update { Instant.EPOCH }
        boosterNotificationService.checkBoosterNotification()
    }

    fun resetLastCheckTime() {
        covidCertificateSettings.lastDccBoosterCheck.update { Instant.EPOCH }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<BoosterTestViewModel>
}
