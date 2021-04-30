package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.contactdiary.DiaryVisitCensor
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import kotlin.concurrent.thread

class DiaryVisitCensorTest : BaseTest() {

    @MockK lateinit var diaryRepo: ContactDiaryRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance(scope: CoroutineScope) = DiaryVisitCensor(
        debugScope = scope,
        diary = diaryRepo
    )

    private fun mockVisit(
        _id: Long,
        _circumstances: String
    ) = mockk<ContactDiaryLocationVisit>().apply {
        every { id } returns _id
        every { circumstances } returns _circumstances
    }

    @Test
    fun `censoring replaces the logline message`() = runBlockingTest {
        every { diaryRepo.locationVisits } returns flowOf(
            listOf(
                mockVisit(1, _circumstances = "Döner that was too spicy"),
                mockVisit(2, _circumstances = "beard shaved without mask"),
                mockVisit(3, _circumstances = "out of toiletpaper")
            )
        )
        val instance = createInstance(this)
        val censorMe = LogLine(
            timestamp = 1,
            priority = 3,
            message =
                """
                After having a Döner that was too spicy,
                I got my beard shaved without mask,
                only to find out the supermarket was out of toiletpaper.
                """.trimIndent(),
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(censorMe) shouldBe censorMe.copy(
            message =
                """
                After having a Visit#1/Circumstances,
                I got my Visit#2/Circumstances,
                only to find out the supermarket was Visit#3/Circumstances.
                """.trimIndent()
        )

        // censoring should still work even after visits are deleted
        every { diaryRepo.locationVisits } returns flowOf(emptyList())
        instance.checkLog(censorMe) shouldBe censorMe.copy(
            message =
                """
                After having a Visit#1/Circumstances,
                I got my Visit#2/Circumstances,
                only to find out the supermarket was Visit#3/Circumstances.
                """.trimIndent()
        )
    }

    @Test
    fun `censoring returns null if all circumstances are blank`() = runBlockingTest {
        every { diaryRepo.locationVisits } returns flowOf(listOf(mockVisit(1, _circumstances = "")))
        val instance = createInstance(this)
        val notCensored = LogLine(
            timestamp = 1,
            priority = 3,
            message = "So many places to visit, but no place like home!",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(notCensored) shouldBe null
    }

    @Test
    fun `censoring returns null if there are no visits no match`() = runBlockingTest {
        every { diaryRepo.locationVisits } returns flowOf(emptyList())
        val instance = createInstance(this)
        val notCensored = LogLine(
            timestamp = 1,
            priority = 3,
            message = "So many places to visit, but no place like home!",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(notCensored) shouldBe null
    }

    @Test
    fun `censoring returns null if the message didn't change`() = runBlockingTest {
        every { diaryRepo.locationVisits } returns flowOf(
            listOf(
                mockVisit(1, _circumstances = "Coffee"),
                mockVisit(2, _circumstances = "fuels the world."),
            )
        )
        val instance = createInstance(this)
        val notCensored = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Wakey wakey, eggs and bakey.",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(notCensored) shouldBe null
    }

    // EXPOSUREAPP-5670 / EXPOSUREAPP-5691
    @Test
    fun `replacement doesn't cause recursion`() {
        every { diaryRepo.locationVisits } returns flowOf(
            listOf(
                mockVisit(1, _circumstances = "Coffee"),
                mockVisit(2, _circumstances = "fuels the world."),
            )
        )

        val logLine = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Lorem ipsum",
            tag = "I'm a tag",
            throwable = null
        )

        var isFinished = false

        thread {
            Thread.sleep(500)
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
