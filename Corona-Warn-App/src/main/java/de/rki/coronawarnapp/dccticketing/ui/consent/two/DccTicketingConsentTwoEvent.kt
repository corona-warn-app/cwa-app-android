package de.rki.coronawarnapp.dccticketing.ui.consent.two

import de.rki.coronawarnapp.util.ui.LazyString

sealed class DccTicketingConsentTwoEvent

object ShowCancelConfirmationDialog : DccTicketingConsentTwoEvent()
data class ShowErrorDialog(val lazyErrorMessage: LazyString) : DccTicketingConsentTwoEvent()

object NavigateToValidationResult : DccTicketingConsentTwoEvent()
object NavigateToPrivacyInformation : DccTicketingConsentTwoEvent()
object NavigateBack : DccTicketingConsentTwoEvent()
object NavigateToHome : DccTicketingConsentTwoEvent()
