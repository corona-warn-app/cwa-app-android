package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.joda.time.Instant
import org.joda.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationRepository @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider
) {

    val vaccinationInfos: Flow<Set<VaccinatedPerson>> = emptyFlow()

    suspend fun registerVaccination(
        qrCode: VaccinationCertificateQRCode
    ): VaccinationCertificate {
        throw NotImplementedError()
    }

    suspend fun vaccinationCertificateFor(certificateId: String): VaccinationCertificate {
        //TODO get certificate from DB
        return VaccinationCertificate(
            firstName = "Max",
            lastName = "Mustermann",
            dateOfBirth = LocalDate.now(),
            vaccinatedAt = Instant.now(),
            vaccinationName = "Comirnaty (mRNA)",
            vaccinationManufacturer = "BioNTech",
            chargeId = "CB2342",
            certificateIssuer = "Landratsamt Potsdam",
            certificateCountry = Country.DE,
            certificateId = "05930482748454836478695764787840"
        )
    }

    fun clear() {
        throw NotImplementedError()
    }

    fun deleteVaccinationCertificate(certificateId: String) =
        scope.launch {
            // TODO delete Vaccination
        }
}
