package de.rki.coronawarnapp.dccticketing.ui.validationresult

sealed class DccTicketingValidationNavigation {
    object Close : DccTicketingValidationNavigation()
    object Done : DccTicketingValidationNavigation()
}
