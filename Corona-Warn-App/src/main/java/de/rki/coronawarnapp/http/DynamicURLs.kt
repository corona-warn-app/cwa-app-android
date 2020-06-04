package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.BuildConfig

object DynamicURLs {
    const val PATTERN_PREFIX_HTTPS = "https://"

    /** CDN URLs for querying against the Server from the Build Config for downloading keys */
    var DOWNLOAD_CDN_URL = BuildConfig.DOWNLOAD_CDN_URL

    /** CDN URLs for querying against the Server from the Build Config for submitting keys */
    var SUBMISSION_CDN_URL = BuildConfig.SUBMISSION_CDN_URL

    var VERIFICATION_CDN_URL = BuildConfig.VERIFICATION_CDN_URL
}
