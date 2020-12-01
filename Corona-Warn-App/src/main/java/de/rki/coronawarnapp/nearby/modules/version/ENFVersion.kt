package de.rki.coronawarnapp.nearby.modules.version

interface ENFVersion {
    /**
     * May return null if the API is currently not connected.
     */
    suspend fun getENFClientVersion(): Long?

    /**
     * Throws an [OutdatedENFVersionException] if the client runs an old unsupported version of the ENF
     * If the API is currently not connected, no exception will be thrown, we expect this to only be a temporary state
     */
    suspend fun requireMinimumVersion(required: Long)

    /**
     * Indicates if the client runs above a certain version
     *
     * @return isAboveVersion, if connected to an old unsupported version, return false
     */
    suspend fun isAtLeast(compareVersion: Long): Boolean

    companion object {
        const val V1_6 = 16000000L
        const val V1_7 = 17000000L
    }
}
