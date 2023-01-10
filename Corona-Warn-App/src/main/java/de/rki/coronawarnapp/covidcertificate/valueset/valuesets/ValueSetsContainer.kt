package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import com.fasterxml.jackson.annotation.JsonProperty
data class ValueSetsContainer(
    @JsonProperty("vaccinationValueSets") val vaccinationValueSets: VaccinationValueSets,
    @JsonProperty("testCertificateValueSets") val testCertificateValueSets: TestCertificateValueSets
)

fun ValueSetsContainer?.isEmpty(): Boolean =
    (this == null) || vaccinationValueSets.isEmpty && testCertificateValueSets.isEmpty

val emptyValueSetsContainer: ValueSetsContainer by lazy {
    ValueSetsContainer(
        vaccinationValueSets = emptyVaccinationValueSets,
        testCertificateValueSets = emptyTestCertificateValueSets
    )
}
