package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId

sealed class DccTicketingCertificateSelectionEvents
object CloseSelectionScreen : DccTicketingCertificateSelectionEvents()
object ShowCancelConfirmationDialog : DccTicketingCertificateSelectionEvents()
data class NavigateToConsentTwoFragment(
    val selectedCertificateContainerId: CertificateContainerId
) : DccTicketingCertificateSelectionEvents()
