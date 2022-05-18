package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.contactdiary.DiaryLocationCensor
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import kotlin.concurrent.thread

@Suppress("MaxLineLength")
class DiaryLocationCensorTest : BaseTest() {

    @MockK lateinit var diaryRepo: ContactDiaryRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance(scope: CoroutineScope) = DiaryLocationCensor(
        debugScope = scope,
        diary = diaryRepo
    )

    private fun mockLocation(
        id: Long,
        name: String,
        phone: String?,
        mail: String?
    ) = mockk<ContactDiaryLocation>().apply {
        every { locationId } returns id
        every { locationName } returns name
        every { phoneNumber } returns phone
        every { emailAddress } returns mail
    }

    @Test
    fun `censoring replaces the logline message`() = runTest {
        every { diaryRepo.locations } returns flowOf(
            listOf(
                mockLocation(1, "Munich", phone = "+49 089 3333", mail = "bürgermeister@münchen.de"),
                mockLocation(2, "Bielefeld", phone = null, mail = null),
                mockLocation(3, "Aachen", phone = "+49 0241 9999", mail = "karl@aachen.de")
            )
        )
        val instance = createInstance(this)
        val censorMe =
            """
            Bürgermeister of Munich (+49 089 3333) and Karl of Aachen [+49 0241 9999] called each other.
            Both agreed that their emails (bürgermeister@münchen.de|karl@aachen.de) are awesome,
            and that Bielefeld doesn't exist as it has neither phonenumber (null) nor email (null).
            """.trimIndent()

        advanceUntilIdle()
        instance.checkLog(censorMe)!!.compile()!!.censored shouldBe
            """
            Bürgermeister of Location#1/Name (Location#1/PhoneNumber) and Karl of Location#3/Name [Location#3/PhoneNumber] called each other.
            Both agreed that their emails (Location#1/EMail|Location#3/EMail) are awesome,
            and that Location#2/Name doesn't exist as it has neither phonenumber (null) nor email (null).
            """.trimIndent()
    }

    @Test
    fun `censoring should still work after locations are deleted`() = runTest {
        every { diaryRepo.locations } returns flowOf(
            listOf(
                mockLocation(1, "Munich", phone = "+49 089 3333", mail = "bürgermeister@münchen.de"),
                mockLocation(2, "Bielefeld", phone = null, mail = null),
                mockLocation(3, "Aachen", phone = "+49 0241 9999", mail = "karl@aachen.de")
            ),
            listOf(
                mockLocation(1, "Munich", phone = "+49 089 3333", mail = "bürgermeister@münchen.de"),
                // Bielefeld was deleted
                mockLocation(3, "Aachen", phone = "+49 0241 9999", mail = "karl@aachen.de")
            )
        )

        val instance = createInstance(this)
        val censorMe =
            """
            Bürgermeister of Munich (+49 089 3333) and Karl of Aachen [+49 0241 9999] called each other.
            Both agreed that their emails (bürgermeister@münchen.de|karl@aachen.de) are awesome,
            and that Bielefeld doesn't exist as it has neither phonenumber (null) nor email (null).
            """.trimIndent()
        advanceUntilIdle()
        instance.checkLog(censorMe)!!.compile()!!.censored shouldBe
            """
            Bürgermeister of Location#1/Name (Location#1/PhoneNumber) and Karl of Location#3/Name [Location#3/PhoneNumber] called each other.
            Both agreed that their emails (Location#1/EMail|Location#3/EMail) are awesome,
            and that Location#2/Name doesn't exist as it has neither phonenumber (null) nor email (null).
            """.trimIndent()
    }

    @Test
    fun `censoring returns null if there are no locations no match`() = runTest {
        every { diaryRepo.locations } returns flowOf(emptyList())
        val instance = createInstance(this)
        val notCensored = "Can't visit many cities during lockdown..."
        instance.checkLog(notCensored) shouldBe null
    }

    @Test
    fun `if message is the same, don't copy the log line`() = runTest {
        every { diaryRepo.locations } returns flowOf(
            listOf(
                mockLocation(1, "Test", phone = null, mail = null),
                mockLocation(2, "Test", phone = null, mail = null),
                mockLocation(3, "Test", phone = null, mail = null)
            )
        )
        val instance = createInstance(this)
        val logLine = "Lorem ipsum"
        instance.checkLog(logLine) shouldBe null
    }

    // EXPOSUREAPP-5670 / EXPOSUREAPP-5691
    @Test
    fun `replacement doesn't cause recursion`() {
        every { diaryRepo.locations } returns flowOf(
            listOf(
                mockLocation(1, "Test", phone = null, mail = null),
                mockLocation(2, "Test", phone = null, mail = null),
                mockLocation(3, "Test", phone = null, mail = null)
            )
        )

        val logLine = "Lorem ipsum"

        var isFinished = false

        thread {
            Thread.sleep(500)
            if (isFinished) return@thread
            Runtime.getRuntime().exit(1)
        }

        runTest {
            val instance = createInstance(this)

            val processedLine = try {
                instance.checkLog(logLine)
            } finally {
                isFinished = true
            }
            processedLine shouldBe null
        }
    }
}
