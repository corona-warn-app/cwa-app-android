package de.rki.coronawarnapp.covidcertificate.signature.core

import org.junit.jupiter.api.Test
import testhelpers.coroutines.runBlockingTest2

internal class DscRepositoryTest {

    @Test
    fun `local cache is loaded on init - no server requests`() = runBlockingTest2(ignoreActive = true) {

    }

    @Test
    fun `refresh talks to server and updates local cache`() = runBlockingTest2(ignoreActive = true) {

    }

    @Test
    fun `bad server response yields exception`() = runBlockingTest2(ignoreActive = true) {

    }
}
