package de.rki.coronawarnapp.datadonation.storage

import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.util.preferences.FlowPreference
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.UUID

class OTPRepositoryTest : BaseTest() {

    @MockK lateinit var dataDonationPreferences: DataDonationPreferences
    @MockK lateinit var flowPreference: FlowPreference<OneTimePassword?>

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { dataDonationPreferences.oneTimePassword } returns flowPreference
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `last otp is read from preferences`() {
        every { flowPreference.value } returns OneTimePassword(UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0"))
        OTPRepository(dataDonationPreferences).lastOTP!!.uuid shouldBe UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0")
    }

    @Test
    fun `no last otp`() {
        every { flowPreference.value } returns null
        OTPRepository(dataDonationPreferences).lastOTP shouldBe null
    }
}
