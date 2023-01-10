package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Locale

data class TestCertificateValueSets(
    @JsonProperty("languageCode") override val languageCode: Locale,
    @JsonProperty("tg") override val tg: DefaultValueSet,
    @JsonProperty("tt") val tt: DefaultValueSet, // Type of Test
    @JsonProperty("ma") val ma: DefaultValueSet, // RAT Test name and manufacturer
    @JsonProperty("tr") val tr: DefaultValueSet, // Test Result
) : ValueSets {
    @get:JsonIgnore
    override val isEmpty: Boolean
        get() = tg.items.isEmpty() && tt.items.isEmpty() && ma.items.isEmpty() && tr.items.isEmpty()

    override fun getDisplayText(key: String): String? =
        tg.getDisplayText(key) ?: tt.getDisplayText(key) ?: ma.getDisplayText(key) ?: tr.getDisplayText(key)

    override fun toString(): String {
        // reduce output for logging
        return "value set for language $languageCode ..."
    }
}

val emptyTestCertificateValueSets: TestCertificateValueSets by lazy {
    TestCertificateValueSets(
        languageCode = Locale.ENGLISH,
        tg = DefaultValueSet(),
        tt = DefaultValueSet(),
        ma = DefaultValueSet(),
        tr = DefaultValueSet()
    )
}
