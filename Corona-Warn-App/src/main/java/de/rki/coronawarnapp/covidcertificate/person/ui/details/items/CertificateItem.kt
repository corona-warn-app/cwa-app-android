package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.lists.HasStableId

interface CertificateItem : HasStableId {

    fun CwaCovidCertificate.toStableListId(): Long = run {
        var result = this::class.hashCode()
        result = 31 * result + certificateId.hashCode()
        result.toLong()
    }
}
