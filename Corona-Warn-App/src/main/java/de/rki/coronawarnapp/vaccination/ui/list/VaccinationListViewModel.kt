package de.rki.coronawarnapp.vaccination.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.INCOMPLETE
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateV1
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.core.server.ProofCertificateV1
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListCertificateCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListIncompleteTopCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListVaccinationCardItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.joda.time.Days
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate

class VaccinationListViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    private val timeStamper: TimeStamper,
    @Assisted private val vaccinatedPersonIdentifier: String
) : CWAViewModel() {

    val uiState: LiveData<UiState> = vaccinationRepository.vaccinationInfos.map { vaccinatedPersonSet ->

        // TODO: use the line below once the repository returns actual values
        // val vaccinatedPerson = vaccinatedPersonSet.single { it.identifier.code == vaccinatedPersonIdentifier }

        // For now, use mock data
        val vaccinationStatus = INCOMPLETE
        // val vaccinationStatus = COMPLETE

        val nameData = VaccinationCertificateV1.NameData(
            givenName = "François-Joan",
            givenNameStandardized = "FRANCOIS<JOAN",
            familyName = "d'Arsøns - van Halen",
            familyNameStandardized = "DARSONS<VAN<HALEN",
        )

        val dateOfBirth = LocalDate.parse("2009-02-28")
        val medicalProductId = "EU/1/20/1528"

        val vaccinationCertificates = setOf(
            VaccinationCertificateV1(
                version = "1.0.0",
                nameData = nameData,
                dateOfBirth = dateOfBirth,
                vaccinationDatas = listOf(
                    VaccinationCertificateV1.VaccinationData(
                        targetId = "840539006",
                        vaccineId = "1119349007",
                        medicalProductId = medicalProductId,
                        marketAuthorizationHolderId = "ORG-100030215",
                        doseNumber = 1,
                        totalSeriesOfDoses = 2,
                        vaccinatedAt = LocalDate.parse("2021-04-22"),
                        countryOfVaccination = "NL",
                        certificateIssuer = "Ministry of Public Health, Welfare and Sport",
                        uniqueCertificateIdentifier = "urn:uvci:01:NL:THECAKEISALIE",
                    )
                )
            ),
            VaccinationCertificateV1(
                version = "1.0.0",
                nameData = nameData,
                dateOfBirth = dateOfBirth,
                vaccinationDatas = listOf(
                    VaccinationCertificateV1.VaccinationData(
                        targetId = "840539006",
                        vaccineId = "1119349007",
                        medicalProductId = medicalProductId,
                        marketAuthorizationHolderId = "ORG-100030215",
                        doseNumber = 2,
                        totalSeriesOfDoses = 2,
                        vaccinatedAt = LocalDate.parse("2021-04-22"),
                        countryOfVaccination = "NL",
                        certificateIssuer = "Ministry of Public Health, Welfare and Sport",
                        uniqueCertificateIdentifier = "urn:uvci:01:NL:THECAKEISALIE",
                    )
                )
            )
        )
        val nameDataProofCertificate = ProofCertificateV1.NameData(
            givenName = "François-Joan",
            givenNameStandardized = "FRANCOIS<JOAN",
            familyName = "d'Arsøns - van Halen",
            familyNameStandardized = "DARSONS<VAN<HALEN",
        )
        val proofCertificates = setOf(
            ProofCertificateV1(
                version = "1.0.0",
                nameData = nameDataProofCertificate,
                dateOfBirth = dateOfBirth,
                vaccinationDatas = listOf(
                    ProofCertificateV1.VaccinationData(
                        targetId = "840539006",
                        vaccineId = "1119349007",
                        medicalProductId = medicalProductId,
                        marketAuthorizationHolderId = "ORG-100030215",
                        doseNumber = 2,
                        totalSeriesOfDoses = 2,
                        vaccinatedAt = LocalDate.parse("2021-04-22"),
                        countryOfVaccination = "DE",
                        certificateIssuer = "Ministry of Public Health, Welfare and Sport",
                        uniqueCertificateIdentifier = "urn:uvci:01:NL:THECAKEISALIE",
                    )
                )
            )
        )

        val listItems = assembleItemList(
            vaccinationCertificates = vaccinationCertificates,
            proofCertificates = proofCertificates,
            firstName = "François-Joan",
            lastName = "d'Arsøns - van Halen",
            dateOfBirth = dateOfBirth,
            vaccinationStatus,
            timeStamper.nowUTC.plus(Duration.standardDays(3))
        )

        UiState(listItems, vaccinationStatus = vaccinationStatus)
    }.catch {
        // TODO Error Handling in an upcoming subtask
    }.asLiveData()

    // TODO: after using actual values from the repository, we only pass VaccinatedPerson here instead of all these
    // arguments
    @Suppress("LongParameterList")
    private fun assembleItemList(
        vaccinationCertificates: Set<VaccinationCertificateV1>,
        proofCertificates: Set<ProofCertificateV1>,
        firstName: String,
        lastName: String,
        dateOfBirth: LocalDate,
        vaccinationStatus: VaccinatedPerson.Status,
        proofExpiresAt: Instant

    ) = mutableListOf<VaccinationListItem>().apply {
        if (vaccinationStatus == COMPLETE) {
            if (proofCertificates.isNotEmpty()) {

                val proofCertificate = proofCertificates.first()
                val expiresAt = proofExpiresAt.toLocalDateUserTz()
                val today = timeStamper.nowUTC.toLocalDateUserTz()
                val remainingValidityInDays = Days.daysBetween(today, expiresAt).days

                add(
                    VaccinationListCertificateCardItem(
                        qrCode = null, // TODO: Generate QR-code
                        remainingValidityInDays = remainingValidityInDays
                    )
                )
            }
        } else {
            add(VaccinationListIncompleteTopCardItem)
        }
        add(
            VaccinationListNameCardItem(
                fullName = "$firstName $lastName",
                dayOfBirth = dateOfBirth.toDayFormat()
            )
        )
        vaccinationCertificates.forEach { vaccinationCertificate ->
            with(vaccinationCertificate.vaccinationDatas.first()) {
                add(
                    VaccinationListVaccinationCardItem(
                        vaccinationCertificateId = uniqueCertificateIdentifier,
                        doseNumber = doseNumber.toString(),
                        totalSeriesOfDoses = totalSeriesOfDoses.toString(),
                        vaccinatedAt = vaccinatedAt.toDayFormat(),
                        vaccinationStatus = vaccinationStatus,
                        isFinalVaccination =
                            doseNumber == totalSeriesOfDoses
                    )
                )
            }
        }
    }.toList()

    data class UiState(
        val listItems: List<VaccinationListItem>,
        val vaccinationStatus: VaccinatedPerson.Status
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationListViewModel> {
        fun create(
            vaccinatedPersonIdentifier: String
        ): VaccinationListViewModel
    }
}
