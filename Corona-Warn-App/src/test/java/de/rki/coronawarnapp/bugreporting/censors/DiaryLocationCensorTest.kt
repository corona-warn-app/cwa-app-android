package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
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

class DiaryLocationCensorTest : BaseTest() {

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

    private fun createInstance(scope: CoroutineScope) = DiaryLocationCensor(
        debugScope = scope,
        diary = diaryRepo
    )

    private fun mockLocation(id: Long, name: String) = mockk<ContactDiaryLocation>().apply {
        every { locationId } returns id
        every { locationName } returns name
    }

    @Test
    fun `censoring replaces the logline message`() = runBlockingTest {
        every { diaryRepo.locations } returns flowOf(
            listOf(mockLocation(1, "Berlin"), mockLocation(2, "Munich"), mockLocation(3, "Aachen"))
        )
        val instance = createInstance(this)
        val censorMe = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Munich is nice, but Aachen is nice too.",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(censorMe) shouldBe censorMe.copy(
            message = "Location#2 is nice, but Location#3 is nice too."
        )

        every { CWADebug.isDeviceForTestersBuild } returns true
        instance.checkLog(censorMe) shouldBe censorMe.copy(
            message = "Munich is nice, but Aachen is nice too."
        )
    }

    @Test
    fun `censoring returns null if there are no locations no match`() = runBlockingTest {
        every { diaryRepo.locations } returns flowOf(emptyList())
        val instance = createInstance(this)
        val notCensored = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Can't visit many cities during lockdown...",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(notCensored) shouldBe null
    }
}
