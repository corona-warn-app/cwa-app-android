package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.contactdiary.DiaryPersonCensor
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
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
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class DiaryPersonCensorTest : BaseTest() {

    @MockK lateinit var diaryRepo: ContactDiaryRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance(scope: CoroutineScope) = DiaryPersonCensor(
        debugScope = scope,
        diary = diaryRepo
    )

    private fun mockPerson(
        id: Long,
        name: String,
        phone: String?,
        mail: String?
    ) = mockk<ContactDiaryPerson>().apply {
        every { personId } returns id
        every { fullName } returns name
        every { phoneNumber } returns phone
        every { emailAddress } returns mail
    }

    @Test
    fun `censoring replaces the logline message`() = runTest {
        every { diaryRepo.people } returns flowOf(
            listOf(
                mockPerson(1, "Luka", phone = "+49 1234 7777", mail = "luka@sap.com"),
                mockPerson(2, "Ralf", phone = null, mail = null),
                mockPerson(3, "Matthias", phone = null, mail = "matthias@sap.com")
            )
        )
        val instance = createInstance(this)
        val censorMe =
            """
            Ralf requested more coffee from +49 1234 7777,
            but Matthias thought he had enough has had enough for today.
            A quick mail to luka@sap.com confirmed this.
            """.trimIndent()
        instance.checkLog(censorMe)!!.compile()!!.censored shouldBe
            """
            Person#2/Name requested more coffee from Person#1/PhoneNumber,
            but Person#3/Name thought he had enough has had enough for today.
            A quick mail to Person#1/EMail confirmed this.
            """.trimIndent()
    }

    @Test
    fun `censoring should still work after people are deleted`() = runTest {
        every { diaryRepo.people } returns flowOf(
            listOf(
                mockPerson(1, "Luka", phone = "+49 1234 7777", mail = "luka@sap.com"),
                mockPerson(2, "Ralf", phone = null, mail = null),
                mockPerson(3, "Matthias", phone = null, mail = "matthias@sap.com")
            ),
            listOf(
                mockPerson(1, "Luka", phone = "+49 1234 7777", mail = "luka@sap.com"),
                mockPerson(2, "Ralf", phone = null, mail = null),
                // Matthias was deleted
            )
        )

        val instance = createInstance(this)
        val censorMe =
            """
            Ralf requested more coffee from +49 1234 7777,
            but Matthias thought he had enough has had enough for today.
            A quick mail to luka@sap.com confirmed this.
            """.trimIndent()

        instance.checkLog(censorMe)!!.compile()!!.censored shouldBe
            """
            Person#2/Name requested more coffee from Person#1/PhoneNumber,
            but Person#3/Name thought he had enough has had enough for today.
            A quick mail to Person#1/EMail confirmed this.
            """.trimIndent()
    }

    @Test
    fun `censoring returns null if there are no persons no match`() = runTest {
        every { diaryRepo.people } returns flowOf(emptyList())
        val instance = createInstance(this)
        val notCensored = "May 2021 be better than 2020."
        instance.checkLog(notCensored) shouldBe null
    }

    @Test
    fun `if message is the same, don't copy the log line`() = runTest {
        every { diaryRepo.people } returns flowOf(
            listOf(
                mockPerson(1, "Test", phone = null, mail = null),
                mockPerson(2, "Test", phone = null, mail = null),
                mockPerson(3, "Test", phone = null, mail = null)
            )
        )
        val instance = createInstance(this)
        val logLine = "Lorem ipsum"
        instance.checkLog(logLine) shouldBe null
    }

    // EXPOSUREAPP-5670 / EXPOSUREAPP-5691
    @Test
    fun `replacement doesn't cause recursion`() {
        every { diaryRepo.people } returns flowOf(
            listOf(
                mockPerson(1, "Test", phone = "", mail = ""),
                mockPerson(2, "Test", phone = "", mail = ""),
                mockPerson(3, "Test", phone = "", mail = "")
            )
        )

        val logLine = "Lorem ipsum"

        var isFinished = false

        thread {
            sleep(500)
            if (isFinished) return@thread
            Runtime.getRuntime().exit(1)
        }

        runBlocking {
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
