package de.rki.coronawarnapp.http.config

import org.junit.Assert
import org.junit.Test

class HTTPVariablesTest {

    @Test
    fun getHTTPConnectionTimeout() {
        Assert.assertEquals(HTTPVariables.getHTTPConnectionTimeout(), 10000L)
    }

    @Test
    fun getHTTPReadTimeout() {
        Assert.assertEquals(HTTPVariables.getHTTPReadTimeout(), 10000L)
    }

    @Test
    fun getHTTPWriteTimeout() {
        Assert.assertEquals(HTTPVariables.getHTTPWriteTimeout(), 10000L)
    }
}
