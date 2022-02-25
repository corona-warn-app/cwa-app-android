package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationMigration @Inject constructor(val vaccinationStorage: VaccinationStorage) {

    suspend fun doMigration() {
        Timber.d("Migration start")
        val vaccinatedPersonData = vaccinationStorage.loadLegacyData()
        vaccinationStorage.save(vaccinatedPersonData.flatMap { it.vaccinations }.toSet())
        vaccinationStorage.clearLegacyData()
    }
}
