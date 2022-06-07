package de.rki.coronawarnapp.covidcertificate.pdf.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import java.time.Instant

class DccExportFilter {

//    1. *Filter by `validity state`:* the `set of DCCs` shall be filtered for those DCCs where the validty state (as per [Determining the Validity State of a DGC]) is one of `VALID`, `EXPIRING_SOON`, `EXPIRED`, or `INVALID`.
//
//    2. *Filter by type-specific criteria:* the `set of DCCs` shall be filtered for those DCCs where
//
//    for Test Certificates, the time difference between the time represented by `t[0].sc` and the current device time is `<=` 72 hours
//    for other certificate types, no additional filter criteria applies (i.e. all certificates pass the filter)
}


fun CertificateProvider.CertificateContainer.filterAndSortForExport(
    nowUtc: Instant
): List<CwaCovidCertificate> {

    vaccinationCwaCertificates.filter {
        it.state is (CwaCovidCertificate.State.Valid)
    }

}
