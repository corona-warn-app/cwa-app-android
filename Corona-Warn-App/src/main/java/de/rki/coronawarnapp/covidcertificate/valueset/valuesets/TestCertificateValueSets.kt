package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.Locale

@Keep
data class TestCertificateValueSets(
    @SerializedName("languageCode") override val languageCode: Locale,
    @SerializedName("tg") override val tg: DefaultValueSet,
    @SerializedName("tt") val tt: DefaultValueSet, // Type of Test
    @SerializedName("ma") val ma: DefaultValueSet, // RAT Test name and manufacturer
    @SerializedName("tr") val tr: DefaultValueSet, // Test Result
) : ValueSets {

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
