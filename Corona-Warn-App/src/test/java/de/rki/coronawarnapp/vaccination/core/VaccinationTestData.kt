package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.qrcode.ScannedVaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.vaccination.core.repository.storage.PersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.ProofContainer
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.vaccination.core.server.ProofCertificateServerData
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Instant
import org.joda.time.LocalDate

object VaccinationTestData {

    val PERSON_A_VAC_1_QRCODE = VaccinationCertificateQRCode(
        certificate = object : ScannedVaccinationCertificate {
            override val firstName get() = "François-Joan"
            override val firstNameStandardized get() = "FRANCOIS<JOAN"
            override val lastName get() = "d'Arsøns - van Halen"
            override val lastNameStandardized get() = "DARSONS<VAN<HALEN"
            override val dateOfBirth get() = LocalDate.parse("2009-02-28")
            override val targetId get() = "840539006"
            override val vaccineId get() = "1119349007"
            override val medicalProductId get() = "EU/1/20/1528"
            override val marketAuthorizationHolderId get() = "ORG-100030215"
            override val doseNumber get() = 1
            override val totalSeriesOfDoses get() = 2
            override val vaccinatedAt get() = LocalDate.parse("2021-04-21")
            override val certificateCountryCode get() = "NL"
            override val certificateIssuer get() = "Ministry of Public Health, Welfare and Sport"
            override val certificateId get() = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
            override val lotNumber get() = "0020617"
        },
        qrCodeOriginalBase45 = "BASE45",
        qrCodeOriginalCBOR = "VGhlIENha2UgaXMgTm90IGEgTGll".decodeBase64()!!
    )

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
        certificateCBOR = "VGhlIGNha2UgaXMgYSBsaWUu".decodeBase64()!!
    )

    val PERSON_A_VAC_2_QRCODE = VaccinationCertificateQRCode(
        certificate = object : ScannedVaccinationCertificate {
            override val firstName get() = "François-Joan"
            override val firstNameStandardized get() = "FRANCOIS<JOAN"
            override val lastName get() = "d'Arsøns - van Halen"
            override val lastNameStandardized get() = "DARSONS<VAN<HALEN"
            override val dateOfBirth get() = LocalDate.parse("2009-02-28")
            override val targetId get() = "840539006"
            override val vaccineId get() = "1119349007"
            override val medicalProductId get() = "EU/1/20/1528"
            override val marketAuthorizationHolderId get() = "ORG-100030215"
            override val doseNumber get() = 2
            override val totalSeriesOfDoses get() = 2
            override val vaccinatedAt get() = PERSON_A_VAC_1_CERT.vaccinatedAt.plusDays(1)
            override val certificateCountryCode get() = "NL"
            override val certificateIssuer get() = "Ministry of Public Health, Welfare and Sport"
            override val certificateId get() = "urn:uvci:01:NL:THECAKEISALIE"
            override val lotNumber get() = "0020617"
        },
        qrCodeOriginalBase45 = "BASE45",
        qrCodeOriginalCBOR = "VGhlIGNha2UgaXMgYSBsaWUu".decodeBase64()!!
    )

    val PERSON_A_VAC_2_CERT = PERSON_A_VAC_1_CERT.copy(
        doseNumber = 2,
        certificateId = "urn:uvci:01:NL:THECAKEISALIE",
        vaccinatedAt = PERSON_A_VAC_1_CERT.vaccinatedAt.plusDays(1)
    )

    val PERSON_A_VAC_2_CONTAINER = VaccinationContainer(
        certificate = PERSON_A_VAC_2_CERT,
        scannedAt = Instant.ofEpochMilli(1620149234473),
        certificateBase45 = "BASE45",
        certificateCBOR = "VGhlIENha2UgaXMgTm90IGEgTGll".decodeBase64()!!
    )

    val PERSON_A_PROOF_1_STORED = ProofContainer(
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
            vaccinatedAt = PERSON_A_VAC_1_CERT.vaccinatedAt.plusDays(1),
            certificateIssuer = "Ministry of Public Health, Welfare and Sport",
            certificateId = "urn:uvci:01:NL:THECAKEISALIE",
        ),
        expiresAt = Instant.ofEpochMilli(1620322034474),
        issuedAt = Instant.ofEpochMilli(1620062834474),
        issuedBy = "DE",
        proofCOSE = "VGhpc0lzQVByb29mQ09TRQ".decodeBase64()!!,
    )

    val PERSON_A_PROOF_1_RESPONSE = object : ProofCertificateServerData {
        override val firstName get() = "François-Joan"
        override val firstNameStandardized get() = "FRANCOIS<JOAN"
        override val lastName get() = "d'Arsøns - van Halen"
        override val lastNameStandardized get() = "DARSONS<VAN<HALEN"
        override val dateOfBirth get() = LocalDate.parse("2009-02-28")
        override val targetId get() = "840539006"
        override val vaccineId get() = "1119349007"
        override val medicalProductId get() = "EU/1/20/1528"
        override val marketAuthorizationHolderId get() = "ORG-100030215"
        override val doseNumber get() = 2
        override val totalSeriesOfDoses get() = 2
        override val vaccinatedAt get() = LocalDate.parse("2021-04-21")
        override val certificateIssuer get() = "Ministry of Public Health, Welfare and Sport"
        override val certificateId get() = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        override val issuerCountryCode: String
            get() = TODO("Not yet implemented")
        override val issuedAt: Instant
            get() = TODO("Not yet implemented")
        override val expiresAt: Instant
            get() = TODO("Not yet implemented")
        override val proofCertificateCBOR: ByteString
            get() = TODO("Not yet implemented")
    }

    val PERSON_A_DATA_2VAC_PROOF = PersonData(
        vaccinations = setOf(
            PERSON_A_VAC_1_CONTAINER,
            PERSON_A_VAC_2_CONTAINER

        ),
        proofs = setOf(
            PERSON_A_PROOF_1_STORED
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
        certificateCBOR = "VGhpc0lzSmFrb2I".decodeBase64()!!
    )

    val PERSON_B_DATA_1VAC_NOPROOF = PersonData(
        vaccinations = setOf(
            PERSON_B_VAC_1_CONTAINER,
        ),
        proofs = emptySet()
    )
}
