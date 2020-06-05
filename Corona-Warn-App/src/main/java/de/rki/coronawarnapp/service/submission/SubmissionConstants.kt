package de.rki.coronawarnapp.service.submission

object SubmissionConstants {
    private const val VERSION = "version"
    private const val REGISTRATION_TOKEN = "registrationToken"
    private const val TEST_RESULT = "testresult"
    private const val TAN = "tan"

    private var CURRENT_VERSION = "v1"

    private val VERSIONED_VERIFICATION_CDN_URL = "$VERSION/$CURRENT_VERSION"

    const val QR_CODE_KEY_TYPE = "GUID"
    const val TELE_TAN_KEY_TYPE = "TELETAN"

    val REGISTRATION_TOKEN_URL = "$VERSIONED_VERIFICATION_CDN_URL/$REGISTRATION_TOKEN"
    val TEST_RESULT_URL = "$VERSIONED_VERIFICATION_CDN_URL/$TEST_RESULT"
    val TAN_REQUEST_URL = "$VERSIONED_VERIFICATION_CDN_URL/$TAN"

    const val MAX_QR_CODE_LENGTH = 150
    const val MAX_GUID_LENGTH = 80
    const val GUID_SEPARATOR = '?'

    const val SERVER_ERROR_CODE_400 = 400
}
