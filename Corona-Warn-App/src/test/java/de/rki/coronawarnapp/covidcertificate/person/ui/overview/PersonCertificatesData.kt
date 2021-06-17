package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates

object PersonCertificatesData {
    val certificatesWithPending = mutableSetOf<PersonCertificates>()
    val certificatesWithUpdating = mutableSetOf<PersonCertificates>()
    val certificatesWithCwaUser = mutableSetOf<PersonCertificates>()
    val certificatesWithoutCwaUser = mutableSetOf<PersonCertificates>()
}
