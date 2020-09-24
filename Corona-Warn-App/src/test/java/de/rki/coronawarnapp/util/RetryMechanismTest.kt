package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.util.RetryMechanism.createDelayCalculator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.instanceOf
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.UUID

class RetryMechanismTest : BaseTest() {
    @MockK lateinit var mockFunction: () -> String

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { mockFunction.invoke() } answers {
            throw RuntimeException(UUID.randomUUID().toString())
        }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `simple retry`() {
        val attempts = mutableListOf<RetryMechanism.Attempt>()
        shouldThrow<RuntimeException> {
            RetryMechanism.retryWithBackOff(
                delayCalculator = createDelayCalculator(),
                delayOperation = { Thread.sleep(it) },
                retryCondition = {
                    attempts.add(it)
                    it.count < 3
                },
                action = mockFunction
            )
        }
        verify(exactly = 3) { mockFunction() }

        attempts[0].apply {
            count shouldBe 1
            totalDelay shouldBe 0L
            lastDelay shouldBe 0L
            exception should instanceOf(RuntimeException::class)
        }
        attempts[1].apply {
            count shouldBe 2
            totalDelay shouldBe lastDelay
            lastDelay shouldBe totalDelay
            exception should instanceOf(RuntimeException::class)
        }
        attempts[2].apply {
            count shouldBe 3
            totalDelay shouldBe attempts[1].totalDelay + lastDelay
            lastDelay shouldBe totalDelay - attempts[1].totalDelay
            exception should instanceOf(RuntimeException::class)
        }
        attempts[0].exception shouldNotBe attempts[1].exception
        attempts[1].exception shouldNotBe attempts[2].exception
    }

    @Test
    fun `test clamping`() {
        val calculator = createDelayCalculator()
        RetryMechanism.Attempt(count = -5, lastDelay = 25).let {
            calculator(it) shouldBe 25
        }
        RetryMechanism.Attempt(count = 100, lastDelay = 3 * 1000L).let {
            calculator(it) shouldBe 3 * 1000L
        }

        RetryMechanism.Attempt(count = 1, lastDelay = 16 * 1000L).let {
            calculator(it) shouldBe 3 * 1000L
        }
        RetryMechanism.Attempt(count = 100, lastDelay = 1).let {
            calculator(it) shouldBe 3 * 1000L
        }
    }
}
