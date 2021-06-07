package de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets

data class ValueSetsContainer(
    val vaccinationValueSets: VaccinationValueSets,
    val testCertificateValueSets: TestCertificateValueSets
)
