package de.rki.coronawarnapp.covidcertificate.common.certificate

val DccV1.asRecoveryCertificate: RecoveryDccV1?
    get() = recoveries?.first()?.let {
        RecoveryDccV1(
            version = version,
            nameData = nameData,
            dateOfBirthFormatted = dateOfBirthFormatted,
            personIdentifier = personIdentifier,
            recovery = it
        )
    }

val DccV1.asVaccinationCertificate: VaccinationDccV1?
    get() = vaccinations?.first()?.let {
        VaccinationDccV1(
            version = version,
            nameData = nameData,
            dateOfBirthFormatted = dateOfBirthFormatted,
            personIdentifier = personIdentifier,
            vaccination = it
        )
    }

val DccV1.asTestCertificate: TestDccV1?
    get() = tests?.first()?.let {
        TestDccV1(
            version = version,
            nameData = nameData,
            dateOfBirthFormatted = dateOfBirthFormatted,
            personIdentifier = personIdentifier,
            test = it
        )
    }

val DccV1.isVaccinationCertificate: Boolean
    get() = this.vaccinations?.isNotEmpty() == true

val DccV1.isTestCertificate: Boolean
    get() = this.tests?.isNotEmpty() == true

val DccV1.isRecoveryCertificate: Boolean
    get() = this.recoveries?.isNotEmpty() == true
