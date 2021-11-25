package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext

sealed class DccTicketingCertificateSelectionEvents
data class NavigateToConsentTwoFragment(
    val transactionContext: DccTicketingTransactionContext,
    val selectedCertificateContainerId: CertificateContainerId
) : DccTicketingCertificateSelectionEvents()
