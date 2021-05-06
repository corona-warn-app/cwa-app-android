package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.RACoronaTestCensor
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class RACoronaTestCensorTest : BaseTest() {

    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createInstance(scope: CoroutineScope) = RACoronaTestCensor(
        debugScope = scope,
        coronaTestRepository = coronaTestRepository
    )

    @Test
    fun `checkLog() should return censored LogLine`() = runBlocking {
        every { coronaTestRepository.coronaTests } returns flowOf(
            setOf(
                mockk<RACoronaTest>().apply {
                    every { firstName } returns "John"
                    every { lastName } returns "Doe"
                    every { dateOfBirth } returns LocalDate.parse("2020-01-01")
                }
            )
        )

        val censor = createInstance(this)

        val logLineToCensor = LogLine(
            timestamp = 1,
            priority = 3,
            message =
                """
                Hello! My name is John. My friends call me Mister Doe and I was born on 2020-01-01.
                """.trimIndent(),
            tag = "I am tag",
            throwable = null
        )

        censor.checkLog(logLineToCensor) shouldBe logLineToCensor.copy(
            message =
                """
                Hello! My name is RATest/FirstName. My friends call me Mister RATest/LastName and I was born on RATest/DateOfBirth.
                """.trimIndent()
        )

        // censoring should still work when test gets deleted
        every { coronaTestRepository.coronaTests } returns flowOf(emptySet())

        censor.checkLog(logLineToCensor) shouldBe logLineToCensor.copy(
            message =
                """
                Hello! My name is RATest/FirstName. My friends call me Mister RATest/LastName and I was born on RATest/DateOfBirth.
                """.trimIndent()
        )
    }

    @Test
    fun `checkLog() should return return null if no corona tests are stored`() = runBlocking {
        every { coronaTestRepository.coronaTests } returns flowOf(emptySet())

        val censor = createInstance(this)

        val logLine = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Lorem ipsum",
            tag = "I'm a tag",
            throwable = null
        )

        censor.checkLog(logLine) shouldBe null
    }

    @Test
    fun `checkLog() should return null if LogLine doesn't need to be censored`() = runBlocking {
        every { coronaTestRepository.coronaTests } returns flowOf(
            setOf(
                mockk<RACoronaTest>().apply {
                    every { firstName } returns "John"
                    every { lastName } returns "Doe"
                    every { dateOfBirth } returns LocalDate.parse("2020-01-01")
                }
            )
        )

        val censor = createInstance(this)

        val logLine = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Lorem ipsum",
            tag = "I'm a tag",
            throwable = null
        )
        censor.checkLog(logLine) shouldBe null
    }

    @Test
    fun `censoring should still work when test gets deleted`() {
    }
}
