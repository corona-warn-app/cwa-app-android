package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class PersonalTestRepositoryExtensionsTest : BaseTest() {

    @MockK lateinit var testRepository: PersonalTestRepository

    private val negativeRatTest = mockk<RACoronaTest> {
        every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { isPositive } returns false
        every { isViewed } returns false
    }

    private val negativePcrTest = mockk<PCRCoronaTest> {
        every { type } returns BaseCoronaTest.Type.PCR
        every { isPositive } returns false
        every { isViewed } returns false
    }

    private val positiveNotViewedRatTest = mockk<RACoronaTest> {
        every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { isPositive } returns true
        every { isViewed } returns false
    }

    private val positiveNotViewedPcrTest = mockk<PCRCoronaTest> {
        every { type } returns BaseCoronaTest.Type.PCR
        every { isPositive } returns true
        every { isViewed } returns false
    }

    private val positiveViewedRatTest = mockk<RACoronaTest> {
        every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
        every { isPositive } returns true
        every { isViewed } returns true
    }

    private val positiveViewedPcrTest = mockk<PCRCoronaTest> {
        every { type } returns BaseCoronaTest.Type.PCR
        every { isPositive } returns true
        every { isViewed } returns true
    }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `getPositiveViewedTests() should return emptyList if the user has no test`() = runBlockingTest {
        every { testRepository.coronaTests } returns flowOf(emptySet())
        testRepository.positiveViewedTests.first() shouldBe emptyList()
    }

    @Test
    fun `getPositiveViewedTests() should return emptyList if the user has no positive test`() = runBlockingTest {
        with(testRepository) {
            every { coronaTests } returns flowOf(setOf(negativeRatTest))
            positiveViewedTests.first() shouldBe emptyList()

            every { coronaTests } returns flowOf(setOf(negativePcrTest))
            positiveViewedTests.first() shouldBe emptyList()

            every { coronaTests } returns flowOf(setOf(negativeRatTest, negativePcrTest))
            positiveViewedTests.first() shouldBe emptyList()
        }
    }

    @Test
    fun `getPositiveViewedTests() should return emptyList if the user has a positive test that is not viewed`() =
        runBlockingTest {
            with(testRepository) {
                every { coronaTests } returns flowOf(setOf(positiveNotViewedRatTest))
                positiveViewedTests.first() shouldBe emptyList()

                every { coronaTests } returns flowOf(setOf(positiveNotViewedPcrTest))
                positiveViewedTests.first() shouldBe emptyList()

                every { coronaTests } returns flowOf(
                    setOf(
                        positiveNotViewedRatTest,
                        positiveNotViewedPcrTest
                    )
                )
                positiveViewedTests.first() shouldBe emptyList()
            }
        }

    @Test
    fun `getPositiveViewedTests() should return tests if the user has a positive tests that are viewed`() =
        runBlockingTest {
            with(testRepository) {
                every { coronaTests } returns flowOf(setOf(positiveViewedRatTest))
                positiveViewedTests.first() shouldBe listOf(positiveViewedRatTest)

                every { coronaTests } returns flowOf(setOf(positiveViewedPcrTest))
                positiveViewedTests.first() shouldBe listOf(positiveViewedPcrTest)

                every { coronaTests } returns flowOf(setOf(positiveViewedRatTest, positiveViewedPcrTest))
                positiveViewedTests.first().apply {
                    contains(positiveViewedRatTest) shouldBe true
                    contains(positiveViewedPcrTest) shouldBe true
                }
            }
        }
}
