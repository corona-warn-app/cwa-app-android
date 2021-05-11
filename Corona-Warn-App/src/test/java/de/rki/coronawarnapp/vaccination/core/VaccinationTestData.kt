package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.util.compression.ZLIBDecompressor
import de.rki.coronawarnapp.util.encoding.decodeBase45
import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1
import de.rki.coronawarnapp.vaccination.core.repository.storage.PersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainer
import okio.internal.commonAsUtf8ToByteArray
import org.joda.time.Instant

object VaccinationTestData {

    val PERSON_A_VAC_1_COSE: RawCOSEObject =
        "HC1:6BFOXN*TS0BI\$ZD.P9UOL97O4-2HH77HRM3DSPTLRR+%3KXH9M9ESIGUBA KWML%6S5B9-+P70Q5VC9:BPCNYKMXEE1JAA/CXGG0JK1WL260X638J3-E3ND3DAJ-43TTTO3HK1H3QBCWNZ83UQJ:T0/8F7V0HKN:Q8.HBV+0SZ4GH00T9UKP0T9WC5PF6846A\$Q\$76QW6%V98T5\$FQMI5DN9QZ5Y0Q\$UPE%5MZ5*T57ZA\$O7T6LEJOA+MZ55EII-EB1EKC422JBBD0D2K.EJJ14B2MP41WTRZPQEC5L64HX6IAS 8S8FT/MAMXP6QS03L0QIRR97I2HOAXL92L0. KOKG8VG5SI:TU+MMPZ55%PBT1YEGEA7IB65C94JBQ2NLEE:NQ% GC3MXHFLF9OIFN0IZ95LJL80P1FDLW452I8941:HH3M41GTNP8EFUNT\$.FTD852IWKP/HLIJL8JF8JF172IMAS EDAHMXFBFBQSKJE72KV\$FHJ%3O%6:XM+1QD+T2/VKKER3L3%1THL7MGY.1S:T:GLOX6OCE7+RWYL3.C-L27WNV0G::M74O%K7C50AAEI4"
            .let { ZLIBDecompressor().decode(it.decodeBase45().toByteArray()) }
            .let { RawCOSEObject(data = it) }

    val PERSON_C_VAC_1_COSE: RawCOSEObject =
        "6BFOXN*TS0BI\$ZD4N9:9S6RCVN5+O30K3/XIV0W23NTDEXWK G2EP4J0BGJLFX3R3VHXK.PJ:2DPF6R:5SVBHABVCNN95SWMPHQUHQN%A0SOE+QQAB-HQ/HQ7IR.SQEEOK9SAI4- 7Y15KBPD34  QWSP0WRGTQFNPLIR.KQNA7N95U/3FJCTG90OARH9P1J4HGZJKBEG%123ZC\$0BCI757TLXKIBTV5TN%2LXK-\$CH4TSXKZ4S/\$K%0KPQ1HEP9.PZE9Q\$95:UENEUW6646936HRTO\$9KZ56DE/.QC\$Q3J62:6LZ6O59++9-G9+E93ZM\$96TV6NRN3T59YLQM1VRMP\$I/XK\$M8PK66YBTJ1ZO8B-S-*O5W41FD\$ 81JP%KNEV45G1H*KESHMN2/TU3UQQKE*QHXSMNV25\$1PK50C9B/9OK5NE1 9V2:U6A1ELUCT16DEETUM/UIN9P8Q:KPFY1W+UN MUNU8T1PEEG%5TW5A 6YO67N6BBEWED/3LS3N6YU.:KJWKPZ9+CQP2IOMH.PR97QC:ACZAH.SYEDK3EL-FIK9J8JRBC7ADHWQYSK48UNZGG NAVEHWEOSUI2L.9OR8FHB0T5HM7I"
            .let { ZLIBDecompressor().decode(it.decodeBase45().toByteArray()) }
            .let { RawCOSEObject(data = it) }

    val PERSON_C_VAC_1_CERTIFICATE = VaccinationDGCV1(
        version = "1.0.0",
        nameData = VaccinationDGCV1.NameData(
            givenName = "Erika Dörte",
            givenNameStandardized = "ERIKA<DOERTE",
            familyName = "Schmitt Mustermann",
            familyNameStandardized = "SCHMITT<MUSTERMANN",
        ),
        dob = "1964-08-12",
        vaccinationDatas = listOf(
            VaccinationDGCV1.VaccinationData(
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
