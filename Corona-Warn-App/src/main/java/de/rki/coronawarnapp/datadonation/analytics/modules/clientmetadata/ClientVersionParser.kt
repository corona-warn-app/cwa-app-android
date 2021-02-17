package de.rki.coronawarnapp.datadonation.analytics.modules.clientmetadata

import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import javax.inject.Inject

@Reusable
class ClientVersionParser @Inject constructor() {
    val appVersionCode: Int get() = BuildConfig.VERSION_CODE

    fun parseClientVersion(): ClientVersion =
        parseClientVersion(appVersionCode)

    fun parseClientVersion(buildNumber: Int): ClientVersion {
        val major = buildNumber / MAJOR_OFFSET
        val minor = buildNumber % MAJOR_OFFSET / MINOR_OFFSET
        val patch = buildNumber % MAJOR_OFFSET % MINOR_OFFSET / PATCH_OFFSET
        val build = buildNumber % MAJOR_OFFSET % MINOR_OFFSET % PATCH_OFFSET

        return ClientVersion(major = major, minor = minor, patch = patch, build = build)
    }

    data class ClientVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val build: Int
    ) {
        fun toPPASemanticVersion(): PpaData.PPASemanticVersion =
            PpaData.PPASemanticVersion.newBuilder()
                .setMajor(major)
                .setMinor(minor)
                .setPatch(patch)
                .build()

        fun toVersionString(): String {
            var versionString = "$major.$minor.$patch"
            if (build != 0) {
                versionString += "-RC$build"
            }

            return versionString
        }
    }

    companion object {
        private const val MAJOR_OFFSET = 1000000
        private const val MINOR_OFFSET = 10000
        private const val PATCH_OFFSET = 100
    }
}
