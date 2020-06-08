package de.rki.coronawarnapp.http.config

object HTTPVariables {
    /**
     * The maximal runtime of a transaction
     * In milliseconds
     */
    private const val HTTP_CONNECTION_TIMEOUT = 10000L

    /**
     * Getter function for [HTTP_CONNECTION_TIMEOUT]
     *
     * @return timeout in milliseconds
     */
    fun getHTTPConnectionTimeout(): Long =
        HTTP_CONNECTION_TIMEOUT

    /**
     * The maximal runtime of a transaction
     * In milliseconds
     */
    private const val HTTP_READ_TIMEOUT = 10000L

    /**
     * Getter function for [HTTP_READ_TIMEOUT]
     *
     * @return timeout in milliseconds
     */
    fun getHTTPReadTimeout(): Long =
        HTTP_READ_TIMEOUT

    /**
     * The maximal runtime of a transaction
     * In milliseconds
     */
    private const val HTTP_WRITE_TIMEOUT = 10000L

    /**
     * Getter function for [HTTP_WRITE_TIMEOUT]
     *
     * @return timeout in milliseconds
     */
    fun getHTTPWriteTimeout(): Long =
        HTTP_WRITE_TIMEOUT
}
