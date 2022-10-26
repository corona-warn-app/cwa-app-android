package de.rki.coronawarnapp.familytest.ui.testlist

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class FamilyTestListViewModelTest : BaseTest() {
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var familyTestRepository: FamilyTestRepository
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var configData: ConfigData

    private lateinit var viewModel: FamilyTestListViewModel

    private val ti = TestIdentifier()

    private val fct1 = mockk<FamilyCoronaTest>().apply {
        every { identifier } returns ti
        every { hasBadge } returns true
    }
    private val fct2 = mockk<FamilyCoronaTest>().apply {
        every { identifier } returns ti
        every { hasBadge } returns true
    }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { familyTestRepository.familyTests } returns flowOf(setOf(fct1, fct2))
        every { timeStamper.nowUTC } returns Instant.parse("2020-11-03T05:35:16.000Z")
        every { appConfigProvider.currentConfig } returns flowOf(configData)

        viewModel = FamilyTestListViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            appConfigProvider = appConfigProvider,
            familyTestRepository = familyTestRepository,
            timeStamper = timeStamper,
            appScope = TestScope()
        )
    }

    @Test
    fun testOnRemoveAllTests() {
        viewModel.onRemoveAllTests()
        viewModel.events.value shouldBe FamilyTestListEvent.ConfirmRemoveAllTests
    }

    @Test
    fun testOnBackPressed() {
        viewModel.onBackPressed()
        viewModel.events.value shouldBe FamilyTestListEvent.NavigateBack
    }

    @Test
    fun testMarkAllTestAsViewed() {

        every { familyTestRepository.familyTests } returns flowOf(setOf(fct1, fct2))
        viewModel.markAllTestAsViewed()

        coVerify {
            familyTestRepository.markAllBadgesAsViewed(listOf(ti, ti))
        }
    }

    @Test
    fun testDeleteTest() {
        viewModel.deleteTest(fct1)

        coVerify {
            familyTestRepository.deleteTest(fct1.identifier)
        }
    }

    @Test
    fun `onRemoveTestConfirmed deletes test with identifier`() {
        viewModel.onRemoveTestConfirmed(fct1)

        coVerify {
            familyTestRepository.moveTestToRecycleBin(fct1.identifier)
        }
    }

    @Test
    fun `onRemoveTestConfirmed deletes all tests`() {
        viewModel.onRemoveTestConfirmed(null)

        coVerify {
            familyTestRepository.moveAllTestsToRecycleBin(listOf(ti, ti))
        }
    }
}
