package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ValueSetsContainer(
    @SerializedName("vaccinationValueSets") val vaccinationValueSets: VaccinationValueSets,
    @SerializedName("testCertificateValueSets") val testCertificateValueSets: TestCertificateValueSets
)

fun ValueSetsContainer?.isEmpty(): Boolean =
    (this == null) || vaccinationValueSets.isEmpty && testCertificateValueSets.isEmpty

val emptyValueSetsContainer: ValueSetsContainer by lazy {
    ValueSetsContainer(
        vaccinationValueSets = emptyVaccinationValueSets,
        testCertificateValueSets = emptyTestCertificateValueSets
    )
}
