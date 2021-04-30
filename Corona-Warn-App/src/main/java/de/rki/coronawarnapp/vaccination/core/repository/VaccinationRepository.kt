package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.vaccination.core.ProofCertificate
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.joda.time.Instant
import org.joda.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationRepository @Inject constructor(
    @AppScope private val scope: CoroutineScope,
) {

    private val vc = VaccinationCertificate(
        firstName = "Max",
        lastName = "Mustermann",
        dateOfBirth = LocalDate.now(),
        vaccinatedAt = Instant.now(),
        vaccineName = "Comirnaty (mRNA)",
        vaccineManufacturer = "BioNTech",
        chargeId = "CB2342",
        certificateIssuer = "Landratsamt Potsdam",
        certificateCountry = Country.DE,
        certificateId = "05930482748454836478695764787840"
    )

    private val vc1 = VaccinationCertificate(
        firstName = "Max",
        lastName = "Mustermann",
        dateOfBirth = LocalDate.now(),
        vaccinatedAt = Instant.now(),
        vaccineName = "Comirnaty (mRNA)",
        vaccineManufacturer = "BioNTech",
        chargeId = "CB2342",
        certificateIssuer = "Landratsamt Potsdam",
        certificateCountry = Country.DE,
        certificateId = "05930482748454836478695764787841"
    )

    private val pc = ProofCertificate(
        expiresAt = Instant.now()
    )

    // TODO read from repos
    val vaccinationInfos: Flow<Set<VaccinatedPerson>> = flowOf(
        setOf(
            VaccinatedPerson(
                setOf(vc),
                setOf(),
                isRefreshing = false,
                lastUpdatedAt = Instant.now()
            ),
            VaccinatedPerson(
                setOf(vc1),
                setOf(pc),
                isRefreshing = false,
                lastUpdatedAt = Instant.now()
            )
        )
    )

    suspend fun registerVaccination(
        qrCode: VaccinationCertificateQRCode
    ): VaccinationCertificate {
        throw NotImplementedError()
    }

    suspend fun clear() {
        throw NotImplementedError()
    }

    suspend fun deleteVaccinationCertificate(vaccinationCertificateId: String) =
        scope.launch {
            // TODO delete Vaccination
        }
}
