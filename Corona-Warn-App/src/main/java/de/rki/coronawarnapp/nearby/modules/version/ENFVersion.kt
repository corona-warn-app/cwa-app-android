package de.rki.coronawarnapp.nearby.modules.version

interface ENFVersion {
    suspend fun getENFClientVersion(): Long?

    /**
     * Indicates if the client runs above a certain version
     *
     * @return isAboveVersion, if connected to an old unsupported version, return false
     */
    suspend fun isAtLeast(compareVersion: Long): Boolean

    companion object {
        const val V16 = 16000000L
    }
}
