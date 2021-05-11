package de.rki.coronawarnapp.vaccination.core.server.proof

import de.rki.coronawarnapp.vaccination.core.common.RawCOSEObject
import de.rki.coronawarnapp.vaccination.core.server.ProofCertificateV1
import org.joda.time.Instant
import org.joda.time.LocalDate

class ProofCertificateCOSEParser {

    fun parse(proofCOSE: RawCOSEObject): ProofCertificateData {
        // TODO
        val cert = ProofCertificateV1(
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
        return ProofCertificateData(
            proofCertificate = cert,
            issuedAt = Instant.EPOCH,
            issuerCountryCode = "DE",
            expiresAt = Instant.EPOCH
        )
    }
}
