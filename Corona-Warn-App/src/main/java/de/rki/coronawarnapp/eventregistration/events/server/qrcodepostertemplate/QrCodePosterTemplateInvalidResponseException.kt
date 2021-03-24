package de.rki.coronawarnapp.eventregistration.events.server.qrcodepostertemplate

class QrCodePosterTemplateInvalidResponseException(
    message: String,
    cause: Exception? = null
) : Exception(message, cause)
