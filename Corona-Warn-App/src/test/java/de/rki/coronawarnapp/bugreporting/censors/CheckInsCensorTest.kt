package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.presencetracing.CheckInsCensor
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class CheckInsCensorTest : BaseTest() {

    @MockK lateinit var checkInsRepo: CheckInRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createInstance(scope: CoroutineScope) = CheckInsCensor(
        debugScope = scope,
        checkInRepository = checkInsRepo
    )

    private fun mockCheckIn(
        checkInId: Long,
        checkInDescription: String,
        checkInAddress: String
    ) = mockk<CheckIn>().apply {
        every { id } returns checkInId
        every { description } returns checkInDescription
        every { address } returns checkInAddress
    }

    @Test
    fun `checkLog() should return LogLine with censored check-in information`() = runTest {
        every { checkInsRepo.allCheckIns } returns flowOf(
            listOf(
                mockCheckIn(
                    checkInId = 1,
                    checkInDescription = "Moe's Tavern",
                    checkInAddress = "Near 742 Evergreen Terrace, 12345 Springfield"
                ),
                mockCheckIn(
                    checkInId = 2,
                    checkInDescription = "Kwik-E-Mart",
                    checkInAddress = "Some Street, 12345 Springfield"
                )
            )
        )

        val censor = createInstance(this)

        val logLineToCensor =
            """
            Let's go to Moe's Tavern in Near 742 Evergreen Terrace, 12345 Springfield.
            Who needs the Kwik-E-Mart in Some Street, 12345 Springfield? I doooo!
            """.trimIndent()

        censor.checkLog(logLineToCensor)!!.compile()!!.censored shouldBe """
            Let's go to CheckIn#1/Description in CheckIn#1/Address.
            Who needs the CheckIn#2/Description in CheckIn#2/Address? I doooo!
        """.trimIndent()
    }

    @Test
    fun `censoring should still work after user deletes his check-ins`() = runTest {
        every { checkInsRepo.allCheckIns } returns flowOf(
            listOf(
                mockCheckIn(
                    checkInId = 1,
                    checkInDescription = "Moe's Tavern",
                    checkInAddress = "Near 742 Evergreen Terrace, 12345 Springfield"
                ),
                mockCheckIn(
                    checkInId = 2,
                    checkInDescription = "Kwik-E-Mart",
                    checkInAddress = "Some Street, 12345 Springfield"
                )
            ),
            listOf(
                mockCheckIn(
                    checkInId = 1,
                    checkInDescription = "Moe's Tavern",
                    checkInAddress = "Near 742 Evergreen Terrace, 12345 Springfield"
                ),
                /* deleted: mockCheckIn(
        checkInId = 2,
        checkInDescription = "Kwik-E-Mart",
        checkInAddress = "Some Street, 12345 Springfield"
    )*/
            )
        )

        val censor = createInstance(this)

        val logLineToCensor =
            """
            Let's go to Moe's Tavern in Near 742 Evergreen Terrace, 12345 Springfield.
            Who needs the Kwik-E-Mart in Some Street, 12345 Springfield? I doooo!
            """.trimIndent()

        censor.checkLog(logLineToCensor)!!.compile()!!.censored shouldBe """
            Let's go to CheckIn#1/Description in CheckIn#1/Address.
            Who needs the CheckIn#2/Description in CheckIn#2/Address? I doooo!
        """.trimIndent()
    }

    @Test
    fun `checkLog() should return null if no check-ins are stored`() = runBlocking {
        every { checkInsRepo.allCheckIns } returns flowOf(emptyList())

        val censor = createInstance(this)
        val logLine = "Some log message that shouldn't be censored."

        censor.checkLog(logLine) shouldBe null
    }

    @Test
    fun `checkLog() should return null if LogLine doesn't need to be censored`() = runBlocking {

        every { checkInsRepo.allCheckIns } returns flowOf(
            listOf(
                mockCheckIn(
                    checkInId = 1,
                    checkInDescription = "Description 1",
                    checkInAddress = "Address 1"
                ),
                mockCheckIn(
                    checkInId = 2,
                    checkInDescription = "Description 2",
                    checkInAddress = "Address 2"
                )
            )
        )

        val censor = createInstance(this)
        val logLine = "Some log message that shouldn't be censored."

        censor.checkLog(logLine) shouldBe null
    }
}
