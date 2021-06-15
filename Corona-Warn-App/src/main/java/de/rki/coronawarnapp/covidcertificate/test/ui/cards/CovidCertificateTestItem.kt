package de.rki.coronawarnapp.covidcertificate.test.ui.cards

import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CertificatesItem
import org.joda.time.Instant

interface CovidCertificateTestItem : CertificatesItem {
    val testDate: Instant

    override val stableId: Long
        get() = testDate.hashCode().toLong()
}
