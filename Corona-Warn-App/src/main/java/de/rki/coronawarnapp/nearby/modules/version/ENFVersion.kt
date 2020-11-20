package de.rki.coronawarnapp.nearby.modules.version

interface ENFVersion {
    suspend fun getENFClientVersion(): Long?

    /**
     * Indicates if the client runs above a certain version
     *
     * @return isAboveVersion, if connected to an old unsupported version, return false
     */
    suspend fun isAtLeast(compareVersion: Long): Boolean

    /**
     * Throws an [UnsupportedENFVersionException] if the client runs an old unsupported version of the ENF
     */
    suspend fun requireAtLeast(compareVersion: Long)

    companion object {
        const val V16 = 16000000L
        const val V15 = 15000000L

        class UnsupportedENFVersionException : Exception("The client runs an old unsupported version of the ENF")
    }
}
