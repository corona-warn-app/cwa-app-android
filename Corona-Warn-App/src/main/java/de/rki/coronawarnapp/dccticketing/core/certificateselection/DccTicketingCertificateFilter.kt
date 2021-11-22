package de.rki.coronawarnapp.dccticketing.core.certificateselection

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class DccTicketingCertificateFilter @Inject constructor(
    vaccinationRepository: VaccinationRepository,
    testCertificateRepository: TestCertificateRepository,
    recoveryCertificateRepository: RecoveryCertificateRepository,
    @AppScope private val appScope: CoroutineScope,
) {
    suspend fun filter(dccTicketingAccessToken: DccTicketingAccessToken): Set<CwaCovidCertificate> {
        // TODO
        return emptySet()
    }
}
