package de.rki.coronawarnapp.ccl.dccwalletinfo.notification

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier

interface DccWalletInfoNotificationService {

    val notificationSenderType: Int

    suspend fun notifyIfNecessary(
        personIdentifier: CertificatePersonIdentifier,
        oldWalletInfo: DccWalletInfo?,
        newWalletInfo: DccWalletInfo
    )
}
