package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.util.encoding.decodeBase45
import de.rki.coronawarnapp.vaccination.core.common.RawCOSEObject
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateData
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateHeader
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateV1
import de.rki.coronawarnapp.vaccination.core.repository.storage.ProofContainer
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.vaccination.core.server.ProofCertificateV1
import de.rki.coronawarnapp.vaccination.core.server.proof.ProofCertificateData
import de.rki.coronawarnapp.vaccination.core.server.proof.ProofCertificateResponse
import de.rki.coronawarnapp.vaccination.decoder.ZLIBDecompressor
import okio.ByteString.Companion.decodeBase64
import okio.internal.commonAsUtf8ToByteArray
import org.joda.time.Instant
import org.joda.time.LocalDate

object VaccinationTestData {

    val PERSON_A_VAC_1_JSON = VaccinationCertificateV1(
        version = "1.0.0",
        nameData = VaccinationCertificateV1.NameData(
            givenName = "François-Joan",
            givenNameStandardized = "FRANCOIS<JOAN",
            familyName = "d'Arsøns - van Halen",
            familyNameStandardized = "DARSONS<VAN<HALEN",
        ),
        dob = "2009-02-28",
        vaccinationDatas = listOf(
            VaccinationCertificateV1.VaccinationData(
                targetId = "840539006",
                vaccineId = "1119349007",
                medicalProductId = "EU/1/20/1528",
                marketAuthorizationHolderId = "ORG-100030215",
                doseNumber = 1,
                totalSeriesOfDoses = 2,
                dt = "2021-04-21",
                countryOfVaccination = "NL",
                certificateIssuer = "Ministry of Public Health, Welfare and Sport",
                uniqueCertificateIdentifier = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
            )
        ),
    )

    val PERSON_A_VAC_1_HEADER = VaccinationCertificateHeader(
        issuer = "Ministry of Public Health, Welfare and Sport",
        issuedAt = Instant.ofEpochMilli(1620149204473),
        expiresAt = Instant.ofEpochMilli(11620149234473)
    )

    val PERSON_A_VAC_1_DATA = VaccinationCertificateData(
        header = PERSON_A_VAC_1_HEADER,
        vaccinationCertificate = PERSON_A_VAC_1_JSON
    )

    val PERSON_A_VAC_1_QRCODE = VaccinationCertificateQRCode(
        parsedData = PERSON_A_VAC_1_DATA,
        certificateCOSE = "VGhlIENha2UgaXMgTm90IGEgTGll".toCOSEObject()
    )

    val PERSON_A_VAC_1_CONTAINER = VaccinationContainer(
        scannedAt = Instant.ofEpochMilli(1620062834471),
        vaccinationCertificateCOSE = "VGhlIGNha2UgaXMgYSBsaWUu".toCOSEObject(),
    ).apply {
        preParsedData = PERSON_A_VAC_1_DATA
    }

    val PERSON_A_VAC_2_JSON = VaccinationCertificateV1(
        version = "1.0.0",
        nameData = VaccinationCertificateV1.NameData(
            givenName = "François-Joan",
            givenNameStandardized = "FRANCOIS<JOAN",
            familyName = "d'Arsøns - van Halen",
            familyNameStandardized = "DARSONS<VAN<HALEN",
        ),
        dob = "2009-02-28",
        vaccinationDatas = listOf(
            VaccinationCertificateV1.VaccinationData(
                targetId = "840539006",
                vaccineId = "1119349007",
                medicalProductId = "EU/1/20/1528",
                marketAuthorizationHolderId = "ORG-100030215",
                doseNumber = 1,
                totalSeriesOfDoses = 2,
                dt = "2021-04-22",
                countryOfVaccination = "NL",
                certificateIssuer = "Ministry of Public Health, Welfare and Sport",
                uniqueCertificateIdentifier = "urn:uvci:01:NL:THECAKEISALIE",
            )
        ),
    )

    val PERSON_A_VAC_2_HEADER = VaccinationCertificateHeader(
        issuer = "Ministry of Public Health, Welfare and Sport",
        issuedAt = Instant.ofEpochMilli(1620149204473),
        expiresAt = Instant.ofEpochMilli(11620149234473)
    )

    val PERSON_A_VAC_2_DATA = VaccinationCertificateData(
        header = PERSON_A_VAC_2_HEADER,
        vaccinationCertificate = PERSON_A_VAC_2_JSON
    )

    val PERSON_A_VAC_2_QRCODE = VaccinationCertificateQRCode(
        parsedData = PERSON_A_VAC_2_DATA,
        certificateCOSE = "VGhlIGNha2UgaXMgYSBsaWUu".toCOSEObject()
    )

    val PERSON_A_VAC_2_CONTAINER = VaccinationContainer(
        scannedAt = Instant.ofEpochMilli(1620149234473),
        vaccinationCertificateCOSE = "VGhlIENha2UgaXMgTm90IGEgTGll".toCOSEObject(),
    ).apply {
        preParsedData = PERSON_A_VAC_2_DATA
    }

    val PERSON_A_PROOF_JSON = ProofCertificateV1(
        version = "1.0.0",
        nameData = ProofCertificateV1.NameData(
            givenName = "François-Joan",
            givenNameStandardized = "FRANCOIS<JOAN",
            familyName = "d'Arsøns - van Halen",
            familyNameStandardized = "DARSONS<VAN<HALEN",
        ),
        dateOfBirth = LocalDate.parse("2009-02-28"),
        vaccinationDatas = listOf(
            ProofCertificateV1.VaccinationData(
                targetId = "840539006",
                vaccineId = "1119349007",
                medicalProductId = "EU/1/20/1528",
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

    val PERSON_A_PROOF_DATA = ProofCertificateData(
        proofCertificate = PERSON_A_PROOF_JSON,
        issuedAt = Instant.EPOCH,
        issuerCountryCode = "DE",
        expiresAt = Instant.EPOCH
    )

    val PERSON_A_PROOF_1_CONTAINER = ProofContainer(
        receivedAt = Instant.ofEpochMilli(1620062834474),
        proofCOSE = RawCOSEObject.EMPTY,
    ).apply {
        preParsedData = PERSON_A_PROOF_DATA
    }

    val PERSON_A_PROOF_1_RESPONSE = object : ProofCertificateResponse {
        override val proofCertificateData: ProofCertificateData
            get() = ProofCertificateData(
                proofCertificate = PERSON_A_PROOF_JSON,
                expiresAt = Instant.EPOCH,
                issuedAt = Instant.EPOCH,
                issuerCountryCode = "DE",
            )
        override val proofCertificateCOSE: RawCOSEObject
            get() = RawCOSEObject("VGhpc0lzQVByb29mQ09TRQ".decodeBase64()!!)
    }

    val PERSON_A_DATA_2VAC_PROOF = VaccinatedPersonData(
        vaccinations = setOf(PERSON_A_VAC_1_CONTAINER, PERSON_A_VAC_2_CONTAINER),
        proofs = setOf(PERSON_A_PROOF_1_CONTAINER),
    )

    val PERSON_B_VAC_1_JSON = VaccinationCertificateV1(
        version = "1.0.0",
        nameData = VaccinationCertificateV1.NameData(
            givenName = "Sir Jakob",
            givenNameStandardized = "SIR<JAKOB",
            familyName = "Von Mustermensch",
            familyNameStandardized = "VON<MUSTERMENSCH",
        ),
        dob = "1996-12-24",
        vaccinationDatas = listOf(
            VaccinationCertificateV1.VaccinationData(
                targetId = "840539006",
                vaccineId = "1119349007",
                medicalProductId = "EU/1/20/1528",
                marketAuthorizationHolderId = "ORG-100030215",
                doseNumber = 1,
                totalSeriesOfDoses = 2,
                dt = "2021-04-21",
                countryOfVaccination = "NL",
                certificateIssuer = "Ministry of Public Health, Welfare and Sport",
                uniqueCertificateIdentifier = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
            )
        )
    )

    val PERSON_B_VAC_1_HEADER = VaccinationCertificateHeader(
        issuer = "Ministry of Public Health, Welfare and Sport",
        issuedAt = Instant.ofEpochMilli(1620149204473),
        expiresAt = Instant.ofEpochMilli(11620149234473)
    )

    val PERSON_B_VAC_1_DATA = VaccinationCertificateData(
        header = PERSON_B_VAC_1_HEADER,
        vaccinationCertificate = PERSON_B_VAC_1_JSON
    )

    val PERSON_B_VAC_1_CONTAINER = VaccinationContainer(
        scannedAt = Instant.ofEpochMilli(1620062834471),
        vaccinationCertificateCOSE = "VGhpc0lzSmFrb2I".toCOSEObject(),
    ).apply {
        preParsedData = PERSON_B_VAC_1_DATA
    }

    val PERSON_B_DATA_1VAC_NOPROOF = VaccinatedPersonData(
        vaccinations = setOf(PERSON_B_VAC_1_CONTAINER),
        proofs = emptySet()
    )

    val PERSON_C_VAC_1_COSE: RawCOSEObject =
        "6BFOXN*TS0BI\$ZD4N9:9S6RCVN5+O30K3/XIV0W23NTDEXWK G2EP4J0BGJLFX3R3VHXK.PJ:2DPF6R:5SVBHABVCNN95SWMPHQUHQN%A0SOE+QQAB-HQ/HQ7IR.SQEEOK9SAI4- 7Y15KBPD34  QWSP0WRGTQFNPLIR.KQNA7N95U/3FJCTG90OARH9P1J4HGZJKBEG%123ZC\$0BCI757TLXKIBTV5TN%2LXK-\$CH4TSXKZ4S/\$K%0KPQ1HEP9.PZE9Q\$95:UENEUW6646936HRTO\$9KZ56DE/.QC\$Q3J62:6LZ6O59++9-G9+E93ZM\$96TV6NRN3T59YLQM1VRMP\$I/XK\$M8PK66YBTJ1ZO8B-S-*O5W41FD\$ 81JP%KNEV45G1H*KESHMN2/TU3UQQKE*QHXSMNV25\$1PK50C9B/9OK5NE1 9V2:U6A1ELUCT16DEETUM/UIN9P8Q:KPFY1W+UN MUNU8T1PEEG%5TW5A 6YO67N6BBEWED/3LS3N6YU.:KJWKPZ9+CQP2IOMH.PR97QC:ACZAH.SYEDK3EL-FIK9J8JRBC7ADHWQYSK48UNZGG NAVEHWEOSUI2L.9OR8FHB0T5HM7I"
            .let { ZLIBDecompressor().decompress(it.decodeBase45().toByteArray()) }
            .let { RawCOSEObject(data = it) }

    val PERSON_C_VAC_1_CERTIFICATE = VaccinationCertificateV1(
        version = "1.0.0",
        nameData = VaccinationCertificateV1.NameData(
            givenName = "Erika Dörte",
            givenNameStandardized = "ERIKA<DOERTE",
            familyName = "Schmitt Mustermann",
            familyNameStandardized = "SCHMITT<MUSTERMANN",
        ),
        dob = "1964-08-12",
        vaccinationDatas = listOf(
            VaccinationCertificateV1.VaccinationData(
                targetId = "840539006",
                vaccineId = "1119349007",
                medicalProductId = "EU/1/20/1528",
                marketAuthorizationHolderId = "ORG-100030215",
                doseNumber = 2,
                totalSeriesOfDoses = 2,
                dt = "2021-02-02",
                countryOfVaccination = "DE",
                certificateIssuer = "Bundesministerium für Gesundheit",
                uniqueCertificateIdentifier = "01DE/84503/1119349007/DXSGWLWL40SU8ZFKIYIBK39A3#S",
            )
        )
    )

    val PERSON_C_VAC_1_CONTAINER = VaccinationContainer(
        scannedAt = Instant.ofEpochMilli(1620062834471),
        vaccinationCertificateCOSE = PERSON_C_VAC_1_COSE,
    )

    val PERSON_C_DATA_1VAC_NOPROOF = VaccinatedPersonData(
        vaccinations = setOf(PERSON_C_VAC_1_CONTAINER),
        proofs = emptySet(),
    )
}

private fun String.toCOSEObject() = RawCOSEObject(data = this.commonAsUtf8ToByteArray())
