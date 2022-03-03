package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.StoredVaccinationCertificateData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationMigration @Inject constructor(private val vaccinationStorage: VaccinationStorage) {

    suspend fun doMigration(): Set<StoredVaccinationCertificateData> {
        Timber.d("Migration start")
        try {
            val vaccinatedPersonData = vaccinationStorage
                .loadLegacyData()
                .flatMap {
                    it.vaccinations
                }
                .toSet()
            if (vaccinatedPersonData.isNotEmpty()) {
                Timber.d("Migrating %d vaccination certificates", vaccinatedPersonData.size)
                vaccinationStorage.save(vaccinatedPersonData)
                vaccinationStorage.clearLegacyData()
            }
            return vaccinatedPersonData
        } catch (e: Exception) {
            Timber.e(e, "Can't migrate vaccination certificates")
        } finally {
            Timber.d("Migration end")
        }
        return emptySet()
    }
}
