package de.rki.coronawarnapp.dccticketing.ui.consent.two

import de.rki.coronawarnapp.util.ui.LazyString

sealed class DccTicketingConsentTwoEvent

object ShowCancelConfirmationDialog : DccTicketingConsentTwoEvent()
data class ShowErrorDialog(val lazyErrorMessage: LazyString) : DccTicketingConsentTwoEvent()

object NavigateToValidationFailed : DccTicketingConsentTwoEvent()
object NavigateToValidationSuccess : DccTicketingConsentTwoEvent()
object NavigateToValidationOpen : DccTicketingConsentTwoEvent()
object NavigateToPrivacyInformation : DccTicketingConsentTwoEvent()
object NavigateBack : DccTicketingConsentTwoEvent()
