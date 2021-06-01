package de.rki.coronawarnapp.coronatest

import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TestCertificateRepositoryTest : BaseTest() {

    @Test
    fun `error if certificate is requested for entry without registered public key`() {
        TODO()
    }

    @Test
    fun `refresh tries public key registration`() {
        TODO()
    }

    @Test
    fun `refresh skips public key registration already registered`() {
        TODO()
    }

    @Test
    fun `refresh tries to obtain dcc from server`() {
        TODO()
    }

    @Test
    fun `refresh skips dcc retrieval if already available`() {
        TODO()
    }

    @Test
    fun `dcc retrieval is delayed according to config if test result is very new`() {
        TODO()
    }
}
