package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

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
    fun `censoring replaces the logline message`() = runBlockingTest {
        every { diaryRepo.people } returns flowOf(
            listOf(
                mockPerson(1, "Luka", phone = "+49 1234 7777", mail = "luka@sap.com"),
                mockPerson(2, "Ralf", phone = null, mail = null),
                mockPerson(3, "Matthias", phone = null, mail = "matthias@sap.com")
            )
        )
        val instance = createInstance(this)
        val censorMe = LogLine(
            timestamp = 1,
            priority = 3,
            message = """
                Ralf requested more coffee from +49 1234 7777,
                but Matthias thought he had enough has had enough for today.
                A quick mail to luka@sap.com confirmed this.
            """.trimIndent(),
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(censorMe) shouldBe censorMe.copy(
            message = """
                Person#2/Name requested more coffee from Person#1/PhoneNumber,
                but Person#3/Name thought he had enough has had enough for today.
                A quick mail to Person#1/EMail confirmed this.
            """.trimIndent()
        )
    }

    @Test
    fun `censoring returns null if there are no persons no match`() = runBlockingTest {
        every { diaryRepo.people } returns flowOf(emptyList())
        val instance = createInstance(this)
        val notCensored = LogLine(
            timestamp = 1,
            priority = 3,
            message = "May 2021 be better than 2020.",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(notCensored) shouldBe null
    }
}
