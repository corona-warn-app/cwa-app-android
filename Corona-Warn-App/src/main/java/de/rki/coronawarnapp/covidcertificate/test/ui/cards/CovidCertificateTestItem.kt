package de.rki.coronawarnapp.covidcertificate.test.ui.cards

import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CertificatesItem
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate

interface CovidCertificateTestItem : CertificatesItem {
    val certificate: TestCertificate

    override val stableId: Long
        get() = certificate.registeredAt.hashCode().toLong()
}
