package de.rki.coronawarnapp.environment

import de.rki.coronawarnapp.BuildConfig

// Can't be const because that prevents them from being mocked in tests
@Suppress("MayBeConstant")
object BuildConfigWrap {

    val ENVIRONMENT_JSONDATA = BuildConfig.ENVIRONMENT_JSONDATA
    val ENVIRONMENT_TYPE_DEFAULT = BuildConfig.ENVIRONMENT_TYPE_DEFAULT
    val ENVIRONMENT_TYPE_ALTERNATIVE = BuildConfig.ENVIRONMENT_TYPE_ALTERNATIVE
}
