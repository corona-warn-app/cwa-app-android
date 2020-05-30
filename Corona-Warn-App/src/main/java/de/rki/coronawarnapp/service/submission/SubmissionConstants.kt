package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.http.DynamicURLs

object SubmissionConstants {
    private const val VERSION = "version"
    private const val REGISTRATION_TOKEN = "registrationToken"
    private const val TEST_RESULT = "testresult"
    private const val TAN = "tan"

    private var CURRENT_VERSION = "v1"

    private var VERIFICATION_CDN_URL = DynamicURLs.VERIFICATION_CDN_URL
    private val VERSIONED_VERIFICATION_CDN_URL = "$VERIFICATION_CDN_URL/$VERSION/$CURRENT_VERSION"

    const val QR_CODE_KEY_TYPE = "GUID"
    const val TELE_TAN__KEY_TYPE = "teleTAN"

    val REGISTRATION_TOKEN_URL = "$VERSIONED_VERIFICATION_CDN_URL/$REGISTRATION_TOKEN"
    val TEST_RESULT_URL = "$VERSIONED_VERIFICATION_CDN_URL/$TEST_RESULT"
    val TAN_REQUEST_URL = "$VERSIONED_VERIFICATION_CDN_URL/$TAN"

    val QR_CODE_VALIDATION_REGEX =
        "[0-9A-Fa-f]{6}-[0-9A-Fa-f]{8}(?:-[0-9A-Fa-f]{4}){3}-[0-9A-Fa-f]{12}".toRegex()
}
