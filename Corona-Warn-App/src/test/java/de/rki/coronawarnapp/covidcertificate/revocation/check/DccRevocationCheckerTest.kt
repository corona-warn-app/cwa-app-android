package de.rki.coronawarnapp.covidcertificate.revocation.check

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class DccRevocationCheckerTest : BaseTest() {

    @Test
    fun `isRevoked - kid is empty`() {
        val dccData = mockk<DccData<*>>().apply { every { kid } returns "" }
        DccRevocationChecker().isRevoked(dccData = dccData, revocationList = listOf()) shouldBe false
    }
}
