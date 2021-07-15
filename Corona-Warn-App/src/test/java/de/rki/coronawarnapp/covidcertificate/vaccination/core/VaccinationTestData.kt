package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccHeader
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainer
import org.joda.time.Instant
import javax.inject.Inject

@Suppress("MaxLineLength")
class VaccinationTestData @Inject constructor(
    private var qrCodeExtractor: DccQrCodeExtractor,
) {

    // AndreasAstra1.pdf
    val personAVac1QRCodeString =
        "HC1:6BFOXN*TS0BI\$ZD.P9UOL97O4-2HH77HRM3DSPTLRR+%3KXH9M9ESIGUBA KWML%6S5B9-+P70Q5VC9:BPCNYKMXEE1JAA/CXGG0JK1WL260X638J3-E3ND3DAJ-43TTTO3HK1H3QBCWNZ83UQJ:T0/8F7V0HKN:Q8.HBV+0SZ4GH00T9UKP0T9WC5PF6846A\$Q$76QW6%V98T5\$FQMI5DN9QZ5Y0Q\$UPE%5MZ5*T57ZA\$O7T6LEJOA+MZ55EII-EB1EKC422JBBD0D2K.EJJ14B2MP41WTRZPQEC5L64HX6IAS 8S8FT/MAMXP6QS03L0QIRR97I2HOAXL92L0. KOKG8VG5SI:TU+MMPZ55%PBT1YEGEA7IB65C94JBQ2NLEE:NQ% GC3MXHFLF9OIFN0IZ95LJL80P1FDLW452I8941:HH3M41GTNP8EFUNT$.FTD852IWKP/HLIJL8JF8JF172IMAS EDAHMXFBFBQSKJE72KV\$FHJ%3O%6:XM+1QD+T2/VKKER3L3%1THL7MGY.1S:T:GLOX6OCE7+RWYL3.C-L27WNV0G::M74O%K7C50AAEI4"

    val personAVac1Certificate = VaccinationDccV1(
        version = "1.0.0",
        nameData = DccV1.NameData(
            givenName = "Andreas",
            givenNameStandardized = "ANDREAS",
            familyName = "Astrá Eins",
            familyNameStandardized = "ASTRA<EINS",
        ),
        dateOfBirthFormatted = "1966-11-11",
        vaccination =
        DccV1.VaccinationData(
            targetId = "840539006",
            vaccineId = "1119305005",
            medicalProductId = "EU/1/21/1529",
            marketAuthorizationHolderId = "ORG-100001699",
            doseNumber = 1,
            totalSeriesOfDoses = 2,
            dt = "2021-03-01",
            certificateCountry = "DE",
            certificateIssuer = "Bundesministerium für Gesundheit - Test01",
            uniqueCertificateIdentifier = "01DE/00001/1119305005/7T1UG87G61Y7NRXIBQJDTYQ9#S",
        ),
        personIdentifier = CertificatePersonIdentifier(
            dateOfBirthFormatted = "1966-11-11",
            lastNameStandardized = "ASTRA<EINS",
            firstNameStandardized = "ANDREAS"
        )
    )

    val personAVac1CertificateHeader = DccHeader(
        issuer = "DE",
        issuedAt = Instant.parse("2021-05-11T09:25:00.000Z"),
        expiresAt = Instant.parse("2022-05-11T09:25:00.000Z"),
    )

    val personAVac1CertificateData = DccData(
        certificate = personAVac1Certificate,
        header = personAVac1CertificateHeader
    )

    val personAVac1QRCode = VaccinationCertificateQRCode(
        qrCode = personAVac1QRCodeString,
        data = personAVac1CertificateData,
    )

    val personAVac1Container = VaccinationContainer(
        scannedAt = Instant.ofEpochMilli(1620062834471),
        vaccinationQrCode = personAVac1QRCodeString,
    ).apply {
        qrCodeExtractor = this@VaccinationTestData.qrCodeExtractor
    }

    // AndreasAstra2.pdf
    val personAVac2QRCodeString =
        "6BFOXN*TS0BI\$ZD.P9UOL97O4-2HH77HRM3DSPTLRR+%3D H9M9ESIGUBA KWMLYX1HXK 0DV:D5VC9:BPCNYKMXEE1JAA/CZIK0JK1WL260X638J3-E3ND3DAJ-43TTTMDF6S8:B73QN VNZ.0K6HYI3CNN96BPHNW*0I85V.499TXY9KK9%OC+G9QJPNF67J6QW67KQ9G66PPM4MLJE+.PDB9L6Q2+PFQ5DB96PP5/P-59A%N+892 7J235II3NJ7PK7SLQMIPUBN9CIZI.EJJ14B2MP41IZRZPQEC5L64HX6IAS 8SAFT/MAMXP6QS03L0QIRR97I2HOAXL92L0. KOKGGVG5SI:TU+MMPZ55%PBT1YEGEA7IB65C94JBQ2NLEE:NQ% GC3MXHFLF9OIFN0IZ95LJL80P1FDLW452I8941:HH3M41GTNP8EFUNT\$.FTD852IWKP/HLIJL8JF8JF172E2JA0K*WDQMPB8T3%KLUSR43M.F\$QBQDR\$VT7V01Y7J0BOZLH+D-QF6MO\$R3%XB+.4QI596GY\$SITJP5BS0DFROC.7B.2RTB*UNYSM$*00HIL+H"

    val personAVac2Certificate = VaccinationDccV1(
        version = "1.0.0",
        nameData = DccV1.NameData(
            givenName = "Andreas",
            givenNameStandardized = "ANDREAS",
            familyName = "Astrá Eins",
            familyNameStandardized = "ASTRA<EINS",
        ),
        dateOfBirthFormatted = "1966-11-11",
        vaccination = DccV1.VaccinationData(
            targetId = "840539006",
            vaccineId = "1119305005",
            medicalProductId = "EU/1/21/1529",
            marketAuthorizationHolderId = "ORG-100001699",
            doseNumber = 2,
            totalSeriesOfDoses = 2,
            dt = "2021-04-27",
            certificateCountry = "DE",
            certificateIssuer = "Bundesministerium für Gesundheit - Test01",
            uniqueCertificateIdentifier = "01DE/00001/1119305005/6IPYBAIDWEWRWW73QEP92FQSN#S",
        ),
        personIdentifier = CertificatePersonIdentifier(
            dateOfBirthFormatted = "1966-11-11",
            lastNameStandardized = "ASTRA<EINS",
            firstNameStandardized = "ANDREAS"
        )
    )

    val personAVac2CertificateHeader = DccHeader(
        issuer = "DE",
        issuedAt = Instant.parse("2021-05-11T09:26:08.000Z"),
        expiresAt = Instant.parse("2022-05-11T09:26:08.000Z"),
    )

    val personAVac2CertificateData = DccData(
        certificate = personAVac2Certificate,
        header = personAVac2CertificateHeader
    )

    val personAVac2QRCode = VaccinationCertificateQRCode(
        qrCode = personAVac2QRCodeString,
        data = personAVac2CertificateData,
    )

    val personAVac2Container = VaccinationContainer(
        scannedAt = Instant.ofEpochMilli(1620069934471),
        vaccinationQrCode = personAVac2QRCodeString,
    ).apply {
        qrCodeExtractor = this@VaccinationTestData.qrCodeExtractor
    }

    val personAData2Vac = VaccinatedPersonData(
        vaccinations = setOf(personAVac1Container, personAVac2Container)
    )

    // BorisJohnson1.pdf
    val personBVac1QRCodeString =
        "HC1:6BFOXN*TS0BI\$ZD.P9UOL97O4-2HH77HRM3DSPTLRR+%3QVH9M9ESIGUBA KWML:SPHXK 0DMYF5VC9:BPCNYKMXEE1JAA/CZIK0JK1WL260X638J3-E3ND3DAJ-43 QTCPFFIJRF3O8H43HX37DUF GFE VMJJYC3SM74E5V.499TXY9KK9+OC+G9QJPNF67J6QW67KQ2G66PPM4MLJE+.PDB9L6Q2+PFQ5DB96PP5/P-59A%N+892 7J235II3NJ7PK7SLQMIPUBN9CIZI.EJJ14B2MP41AZRSEQEC5L64HX6IAS3DS2980IQ.DPUHLW\$GAHLW 70SO:GOLIROGO3T59YLQM14+OP\$I/XK\$M8CL6PZB*L8PK99Q9E\$BDZIF9J8-I\$GI0 J1ALL:F71APC9*KF6LF/NLR/FZ.COKEH-BB4OQ9OG4C5AO**HOELK2AZ7LBLEH-BHPLV5GK3DNKE\$JDVPLW1KD0KCZG.M1LUSB5BCQRJ\$DB5N9%V/GO4IHIBBJ-BI%NWRS%LR%\$KR46325NABFDDAFHD9PZP11COD5U*2KQXCA5W8HH/K51DQO8O0-SOSENFH9101U8$3"

    val personBVac1Certificate = VaccinationDccV1(
        version = "1.0.0",
        nameData = DccV1.NameData(
            givenName = "Boris",
            givenNameStandardized = "BORIS",
            familyName = "Johnson Gültig",
            familyNameStandardized = "JOHNSON<GUELTIG",
        ),
        dateOfBirthFormatted = "1966-11-11",
        vaccination = DccV1.VaccinationData(
            targetId = "840539006",
            vaccineId = "1119305005",
            medicalProductId = "EU/1/20/1525",
            marketAuthorizationHolderId = "ORG-100001417",
            doseNumber = 1,
            totalSeriesOfDoses = 1,
            dt = "2021-04-20",
            certificateCountry = "DE",
            certificateIssuer = "Bundesministerium für Gesundheit - Test01",
            uniqueCertificateIdentifier = "01DE/00001/1119305005/3H24U2KVOTPCSINK7N64F2OB9#S",
        ),
        personIdentifier = CertificatePersonIdentifier(
            dateOfBirthFormatted = "1966-11-11",
            lastNameStandardized = "JOHNSON<GUELTIG",
            firstNameStandardized = "BORIS"
        )
    )

    val personBVac1CertificateHeader = DccHeader(
        issuer = "DE",
        issuedAt = Instant.parse("2021-05-11T09:23:03.000Z"),
        expiresAt = Instant.parse("2022-05-11T09:23:03.000Z"),
    )

    val personBVac1CertificateData = DccData(
        certificate = personBVac1Certificate,
        header = personBVac1CertificateHeader
    )

    val personBVac1QRCode = VaccinationCertificateQRCode(
        qrCode = personBVac1QRCodeString,
        data = personBVac1CertificateData,
    )

    val personBVac1Container = VaccinationContainer(
        scannedAt = Instant.ofEpochMilli(1620069934471),
        vaccinationQrCode = personBVac1QRCodeString,
    ).apply {
        qrCodeExtractor = this@VaccinationTestData.qrCodeExtractor
    }

    val personBData1Vac = VaccinatedPersonData(
        vaccinations = setOf(personBVac1Container)
    )

    val personXVac1ContainerBadCountryData = VaccinationContainer(
        scannedAt = Instant.ofEpochMilli(1620062834471),
        vaccinationQrCode = VaccinationQrCodeTestData.qrCodeWithNonsenseCountry,
    ).apply {
        qrCodeExtractor = this@VaccinationTestData.qrCodeExtractor
    }

    val personYVacTwoEntriesQrCode =
        "HC1:6BFOXN%TSMAHN-HVN8J7UQMJ4/36 L-AHQ+R1WG%MP8*ICG5QKM0658WAULO8NASA3/-2E%5G%5TW5A 6YO6XL6Q3QR\$P*NI92KV6TKOJ06JYZJV1JJ7UGOJUTIJ7J:ZJ83BL8TFVTV9T.ZJC0J*PIZ.TJ STPT*IJ5OI9YI:8DJ:D%PDDIKIWCHAB.YMAHLW 70SO:GOLIROGO3T59YLY1S7HOPC5NDOEC5/64ND7BT5PE4D/5:/6N9R%EPXCROGO+GOVIR-PQ395R4IUHLW\$G-B5ET42HPPEPHCR6W97DON95N14Q6SP+PJD1W9L \$N3-Q.VBAO8MN9*QHAO96Y2/*13A5-8E6V59I9BZK6:IZW4I:A6J3ARN QT1BGL4OMJKR.K\$A1EB14UVC2O+5T3.CE1M33KS2JKA8Y*99CCLLOR/CH0GRP8 GLY 1LA7551DC2U.NVOTJOII:8DKEK%N92T9YQ$0MK%P6\$G9K7QQUY9KI.EK*8XRS-DPA5W64SMVR1NF6D0 2S0.7R:ASENTI094PIDS:T32DRE8N"

    val personYVacTwoEntriesContainer = VaccinationContainer(
        scannedAt = Instant.ofEpochMilli(1620062834471),
        vaccinationQrCode = personYVacTwoEntriesQrCode,
    ).apply {
        qrCodeExtractor = this@VaccinationTestData.qrCodeExtractor
    }
}
