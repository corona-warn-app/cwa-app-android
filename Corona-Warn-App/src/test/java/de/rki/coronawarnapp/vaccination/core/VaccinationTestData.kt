package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.repository.storage.PersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.ProofContainer
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainer
import org.joda.time.Instant
import org.joda.time.LocalDate

object VaccinationTestData {
    val PERSON_A_VAC_1_CERT = VaccinationContainer.StoredCertificate(
        firstName = "François-Joan",
        firstNameStandardized = "FRANCOIS<JOAN",
        lastName = "d'Arsøns - van Halen",
        lastNameStandardized = "DARSONS<VAN<HALEN",
        dateOfBirth = LocalDate.parse("2009-02-28"),
        targetId = "840539006",
        vaccineId = "1119349007",
        medicalProductId = "EU/1/20/1528",
        marketAuthorizationHolderId = "ORG-100030215",
        doseNumber = 1,
        totalSeriesOfDoses = 2,
        vaccinatedAt = LocalDate.parse("2021-04-21"),
        certificateCountryCode = "NL",
        certificateIssuer = "Ministry of Public Health, Welfare and Sport",
        certificateId = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
        lotNumber = "0020617",
    )
    val PERSON_A_VAC_1_CONTAINER = VaccinationContainer(
        certificate = PERSON_A_VAC_1_CERT,
        scannedAt = Instant.ofEpochMilli(1620062834471),
        certificateBase45 = "BASE45",
        certificateCBORBase64 = "BASE64"
    )

    val PERSON_A_VAC_2_CERT = PERSON_A_VAC_1_CERT.copy(
        doseNumber = 2,
        vaccinatedAt = PERSON_A_VAC_1_CERT.vaccinatedAt.plusDays(1)
    )

    val PERSON_A_VAC_2_CONTAINER = VaccinationContainer(
        certificate = PERSON_A_VAC_2_CERT,
        scannedAt = Instant.ofEpochMilli(1620149234473),
        certificateBase45 = "BASE45",
        certificateCBORBase64 = "BASE64"
    )

    val PERSON_A_STORED_PROOF_1 = ProofContainer(
        proof = ProofContainer.StoredProof(
            firstName = "François-Joan",
            firstNameStandardized = "FRANCOIS<JOAN",
            lastName = "d'Arsøns - van Halen",
            lastNameStandardized = "DARSONS<VAN<HALEN",
            dateOfBirth = LocalDate.parse("2009-02-28"),
            targetId = "840539006",
            vaccineId = "1119349007",
            medicalProductId = "EU/1/20/1528",
            marketAuthorizationHolderId = "ORG-100030215",
            doseNumber = 2,
            totalSeriesOfDoses = 2,
            vaccinatedAt = LocalDate.parse("2021-04-21"),
            certificateIssuer = "Ministry of Public Health, Welfare and Sport",
            certificateId = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
        ),
        expiresAt = Instant.ofEpochMilli(1620322034474),
        issuedAt = Instant.ofEpochMilli(1620062834474),
        issuedBy = "DE",
        proofCOSEBase64 = "BASE64",
    )

    val PERSON_A_DATA_2VAC_PROOF = PersonData(
        vaccinations = setOf(
            PERSON_A_VAC_1_CONTAINER,
            PERSON_A_VAC_2_CONTAINER

        ),
        proofs = setOf(
            PERSON_A_STORED_PROOF_1
        ),
    )

    val PERSON_B_VAC_1_CERT = VaccinationContainer.StoredCertificate(
        firstName = "Sir Jakob",
        firstNameStandardized = "SIR<JAKOB",
        lastName = "Von Mustermensch",
        lastNameStandardized = "VON<MUSTERMENSCH",
        dateOfBirth = LocalDate.parse("1996-12-24"),
        targetId = "840539006",
        vaccineId = "1119349007",
        medicalProductId = "EU/1/20/1528",
        marketAuthorizationHolderId = "ORG-100030215",
        doseNumber = 1,
        totalSeriesOfDoses = 2,
        vaccinatedAt = LocalDate.parse("2021-04-21"),
        certificateCountryCode = "NL",
        certificateIssuer = "Ministry of Public Health, Welfare and Sport",
        certificateId = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
        lotNumber = null,
    )

    val PERSON_B_VAC_1_CONTAINER = VaccinationContainer(
        certificate = PERSON_B_VAC_1_CERT,
        scannedAt = Instant.ofEpochMilli(1620062834471),
        certificateBase45 = "BASE45",
        certificateCBORBase64 = "BASE64"
    )

    val PERSON_B_DATA_1VAC_NOPROOF = PersonData(
        vaccinations = setOf(
            PERSON_B_VAC_1_CONTAINER,
        ),
        proofs = emptySet()
    )
}
