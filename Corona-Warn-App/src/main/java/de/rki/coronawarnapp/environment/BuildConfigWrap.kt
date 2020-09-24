package de.rki.coronawarnapp.environment

import de.rki.coronawarnapp.BuildConfig

// Can't be const because that prevents them from being mocked in tests
@Suppress("MayBeConstant")
object BuildConfigWrap {

    val TEST_ENVIRONMENT_JSONDATA = BuildConfig.TEST_ENVIRONMENT_JSONDATA
    val TEST_ENVIRONMENT_DEFAULTTYPE = BuildConfig.TEST_ENVIRONMENT_DEFAULT_TYPE
    val DOWNLOAD_CDN_URL = BuildConfig.DOWNLOAD_CDN_URL
    val SUBMISSION_CDN_URL = BuildConfig.SUBMISSION_CDN_URL
    val VERIFICATION_CDN_URL = BuildConfig.VERIFICATION_CDN_URL
}
