package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationRepository @Inject constructor() {

    val vaccinationInfos: Flow<Set<VaccinatedPerson>> = emptyFlow()

    suspend fun registerVaccination(
        qrCode: VaccinationCertificateQRCode
    ): VaccinationCertificate {
        throw NotImplementedError()
    }

    fun clear() {
        throw NotImplementedError()
    }
}
