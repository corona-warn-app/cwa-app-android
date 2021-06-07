package de.rki.coronawarnapp.greencertificate.ui.certificates.cards

import de.rki.coronawarnapp.greencertificate.ui.certificates.items.CertificatesItem
import org.joda.time.Instant

interface CovidCertificateTestItem : CertificatesItem {
    val testDate: Instant

    override val stableId: Long
        get() = testDate.hashCode().toLong()
}
