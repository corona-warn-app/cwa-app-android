package de.rki.coronawarnapp.dccticketing.ui.consent.one

sealed class DccTicketingConsentOneEvent

object ShowCancelConfirmationDialog: DccTicketingConsentOneEvent()

object NavigateToCertificateSelection: DccTicketingConsentOneEvent()
object NavigateToPrivacyInformation: DccTicketingConsentOneEvent()
object NavigateBack: DccTicketingConsentOneEvent()
