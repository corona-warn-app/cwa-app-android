package de.rki.coronawarnapp.vaccination.ui.cards

import de.rki.coronawarnapp.greencertificate.ui.certificates.items.CertificatesItem
import org.joda.time.Instant

interface VaccinationTestItem : CertificatesItem {
    val testDate: Instant

    override val stableId: Long
        get() = testDate.hashCode().toLong()
}
