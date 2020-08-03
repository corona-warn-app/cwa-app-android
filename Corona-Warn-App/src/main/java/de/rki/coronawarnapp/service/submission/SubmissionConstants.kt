package de.rki.coronawarnapp.service.submission

object SubmissionConstants {
    private const val VERSION = "version"
    private const val REGISTRATION_TOKEN = "registrationToken"
    private const val TEST_RESULT = "testresult"
    private const val TAN = "tan"

    private var CURRENT_VERSION = "v1"

    private val VERSIONED_VERIFICATION_CDN_URL = "$VERSION/$CURRENT_VERSION"

    val REGISTRATION_TOKEN_URL = "$VERSIONED_VERIFICATION_CDN_URL/$REGISTRATION_TOKEN"
    val TEST_RESULT_URL = "$VERSIONED_VERIFICATION_CDN_URL/$TEST_RESULT"
    val TAN_REQUEST_URL = "$VERSIONED_VERIFICATION_CDN_URL/$TAN"

    const val MAX_QR_CODE_LENGTH = 150
    const val MAX_GUID_LENGTH = 80
    const val GUID_SEPARATOR = '?'

    const val SERVER_ERROR_CODE_400 = 400

    // padding registration token
    private const val VERIFICATION_BODY_FILL = 889

    const val PADDING_LENGTH_HEADER_REGISTRATION_TOKEN = 0
    const val PADDING_LENGTH_BODY_REGISTRATION_TOKEN_FAKE = 90 + VERIFICATION_BODY_FILL
    const val PADDING_LENGTH_BODY_REGISTRATION_TOKEN_TELETAN = 51 + VERIFICATION_BODY_FILL
    const val PADDING_LENGTH_BODY_REGISTRATION_TOKEN_GUID = 0 + VERIFICATION_BODY_FILL

    // padding test result
    const val PADDING_LENGTH_HEADER_TEST_RESULT = 7
    const val PADDING_LENGTH_BODY_TEST_RESULT_FAKE = 90 + VERIFICATION_BODY_FILL
    const val PADDING_LENGTH_BODY_TEST_RESULT = 31 + VERIFICATION_BODY_FILL

    // padding tan
    const val PADDING_LENGTH_HEADER_TAN = 14
    const val PADDING_LENGTH_BODY_TAN = 31 + VERIFICATION_BODY_FILL
    const val PADDING_LENGTH_BODY_TAN_FAKE = 90 + VERIFICATION_BODY_FILL

    // padding submission
    const val PADDING_LENGTH_HEADER_SUBMISSION = 1
    const val PADDING_LENGTH_HEADER_SUBMISSION_FAKE = 60

    const val probabilityToExecutePlaybookWhenOpenApp = 1f
    const val minNumberOfSequentialPlaybooks = 1
    const val maxNumberOfSequentialPlaybooks = 3
    const val minDelayBetweenSequentialPlaybooks = 5
    const val maxDelayBetweenSequentialPlaybooks = 10
    const val numberOfDaysToRunPlaybook = 16
}

enum class KeyType {
    GUID, TELETAN;
}
