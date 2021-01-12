package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DiaryPersonCensorTest : BaseTest() {

    @MockK lateinit var diaryRepo: ContactDiaryRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(CWADebug)
        every { CWADebug.isDeviceForTestersBuild } returns false
    }

    @AfterEach
    fun teardown() {
        QRCodeCensor.lastGUID = null
        clearAllMocks()
    }

    private fun createInstance(scope: CoroutineScope) = DiaryPersonCensor(
        debugScope = scope,
        diary = diaryRepo
    )

    private fun mockPerson(id: Long, name: String) = mockk<ContactDiaryPerson>().apply {
        every { personId } returns id
        every { fullName } returns name
    }

    @Test
    fun `censoring replaces the logline message`() = runBlockingTest {
        every { diaryRepo.people } returns flowOf(
            listOf(mockPerson(1, "Luka"), mockPerson(2, "Ralf"), mockPerson(3, "Matthias"))
        )
        val instance = createInstance(this)
        val censorMe = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Ralf needs more coffee, but Matthias has had enough for today.",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(censorMe) shouldBe censorMe.copy(
            message = "Person#2 needs more coffee, but Person#3 has had enough for today."
        )

        every { CWADebug.isDeviceForTestersBuild } returns true
        instance.checkLog(censorMe) shouldBe censorMe.copy(
            message = "Ralf needs more coffee, but Matthias has had enough for today."
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
