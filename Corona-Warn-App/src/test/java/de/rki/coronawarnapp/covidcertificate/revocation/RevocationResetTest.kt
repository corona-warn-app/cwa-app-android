package de.rki.coronawarnapp.covidcertificate.revocation

import de.rki.coronawarnapp.covidcertificate.revocation.server.RevocationServer
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class RevocationResetTest : BaseTest() {

    @RelaxedMockK lateinit var revocationServer: RevocationServer
    @RelaxedMockK lateinit var revocationRepository: RevocationRepository

    private val instance: RevocationReset
        get() = RevocationReset(
            revocationServer = revocationServer,
            revocationRepository = revocationRepository
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `clears all data`() = runBlockingTest2 {
        instance.clear()

        coVerify {
            revocationServer.clearCache()
            revocationRepository.clear()
        }
    }
}
