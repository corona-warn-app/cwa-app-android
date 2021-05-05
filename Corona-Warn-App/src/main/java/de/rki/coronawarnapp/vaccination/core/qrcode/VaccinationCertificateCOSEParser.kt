package de.rki.coronawarnapp.vaccination.core.qrcode

import okio.ByteString
import org.joda.time.LocalDate

class VaccinationCertificateCOSEParser {

    fun parse(vaccinationCOSE: ByteString): VaccinationCertificateData {
        // TODO
        val cert = VaccinationCertificateV1(
            version = "1.0.0",
            nameData = VaccinationCertificateV1.NameData(
                givenName = "François-Joan",
                givenNameStandardized = "FRANCOIS<JOAN",
                familyName = "d'Arsøns - van Halen",
                familyNameStandardized = "DARSONS<VAN<HALEN",
            ),
            dateOfBirth = LocalDate.parse("2009-02-28"),
            vaccinationDatas = listOf(
                VaccinationCertificateV1.VaccinationData(
                    targetId = "840539006",
                    vaccineId = "1119349007",
                    medicalProductId = "EU/1/20/1528",
                    marketAuthorizationHolderId = "ORG-100030215",
                    doseNumber = 1,
                    totalSeriesOfDoses = 2,
                    vaccinatedAt = LocalDate.parse("2021-04-21"),
                    countryOfVaccination = "NL",
                    certificateIssuer = "Ministry of Public Health, Welfare and Sport",
                    uniqueCertificateIdentifier = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
                )
            ),
        )

        return VaccinationCertificateData(
            vaccinationCertificate = cert
        )
    }
}
