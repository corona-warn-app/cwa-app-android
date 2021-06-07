package de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets

import java.util.Locale

data class DefaultTestCertificateValueSets(
    override val tt: ValueSets.ValueSet,
    override val ma: ValueSets.ValueSet,
    override val tr: ValueSets.ValueSet,
    override val languageCode: Locale,
    override val tg: ValueSets.ValueSet
) : TestCertificateValueSets {
    override fun getDisplayText(key: String): String? =
        tg.getDisplayText(key) ?: tt.getDisplayText(key) ?: ma.getDisplayText(key) ?: tr.getDisplayText(key)
}

