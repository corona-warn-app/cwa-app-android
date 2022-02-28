package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationMigration @Inject constructor(val vaccinationStorage: VaccinationStorage) {

    suspend fun doMigration() {
        Timber.d("Migration start")
        try {
            val vaccinatedPersonData = vaccinationStorage.loadLegacyData()
            if (vaccinatedPersonData.isNotEmpty()) {
                Timber.d("Migrating %d vaccination certificates", vaccinatedPersonData.size)
                vaccinationStorage.save(vaccinatedPersonData.flatMap { it.vaccinations }.toSet())
                vaccinationStorage.clearLegacyData()
            }
        } catch (e: Exception) {
            Timber.e(e, "Can't migrate vaccination certificates")
        }
        Timber.d("Migration end")
    }
}
