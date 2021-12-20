package de.rki.coronawarnapp.dccticketing.ui.consent.one

import de.rki.coronawarnapp.util.ui.LazyString

sealed class DccTicketingConsentOneEvent

object ShowCancelConfirmationDialog : DccTicketingConsentOneEvent()
data class ShowErrorDialog(val lazyErrorMessage: LazyString) : DccTicketingConsentOneEvent()
object NavigateToCertificateSelection : DccTicketingConsentOneEvent()
object NavigateToPrivacyInformation : DccTicketingConsentOneEvent()
object NavigateBack : DccTicketingConsentOneEvent()
