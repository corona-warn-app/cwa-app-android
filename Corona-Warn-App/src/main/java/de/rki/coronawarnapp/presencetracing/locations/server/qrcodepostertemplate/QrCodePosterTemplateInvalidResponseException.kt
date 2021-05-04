package de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate

class QrCodePosterTemplateInvalidResponseException(
    message: String,
    cause: Exception? = null
) : Exception(message, cause)
