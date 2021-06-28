package de.rki.coronawarnapp.covidcertificate.test.ui.cards

import de.rki.coronawarnapp.covidcertificate.test.ui.items.CertificatesItem
import org.joda.time.DateTime

interface CovidCertificateTestItem : CertificatesItem {
    val testDate: DateTime

    override val stableId: Long
        get() = testDate.hashCode().toLong()
}
