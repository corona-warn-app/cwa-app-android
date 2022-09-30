package de.rki.coronawarnapp.rootdetection.core

import com.scottyab.rootbeer.RootBeer
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2

class RootDetectionCheckTest : BaseTest() {

    @MockK private lateinit var rootBeer: RootBeer
    @MockK private lateinit var cwaSettings: CWASettings

    private val testDispatcherProvider = TestDispatcherProvider()

    private fun createInstance(scope: CoroutineScope) = RootDetectionCheck(
        rootBeer = rootBeer,
        dispatcherProvider = testDispatcherProvider,
        cwaSettings = cwaSettings,
        scope = scope
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_CODE } returns 0L

        every { cwaSettings.lastSuppressRootInfoVersionCode } returns flowOf(0L)
        coEvery { cwaSettings.updateLastSuppressRootInfoVersionCode(any()) } just Runs
    }

    @Test
    fun `device is rooted`() = runTest2 {
        every { rootBeer.isRooted } returns true
        createInstance(this).isRooted() shouldBe true
        verify { rootBeer.isRooted }
    }

    @Test
    fun `device is not rooted`() = runTest2 {
        every { rootBeer.isRooted } returns false
        createInstance(this).isRooted() shouldBe false
        verify { rootBeer.isRooted }
    }

    @Test
    fun `fallback to false in case of an error`() = runTest2 {
        every { rootBeer.isRooted } throws Exception("Test error")
        createInstance(this).isRooted() shouldBe false
        verify { rootBeer.isRooted }
    }

    @Test
    fun `device is rooted and current version is greater than last suppress root info version code`() =
        runTest2 {
            every { cwaSettings.lastSuppressRootInfoVersionCode } returns flowOf(9L)
            every { BuildConfigWrap.VERSION_CODE } returns 10L

            coEvery { rootBeer.isRooted } returns true

            createInstance(this).shouldShowRootInfo() shouldBe true

            coVerify {
                rootBeer.isRooted
            }
        }

    @Test
    fun `device is not rooted and current version is greater than last suppress root info version code`() =
        runTest2 {
            every { cwaSettings.lastSuppressRootInfoVersionCode } returns flowOf(9L)
            every { BuildConfigWrap.VERSION_CODE } returns 10L

            coEvery { rootBeer.isRooted } returns false

            createInstance(this).shouldShowRootInfo() shouldBe false

            coVerify {
                rootBeer.isRooted
            }
        }

    @Test
    fun `device is rooted but current version is less than or equal to last suppress root info version code`() =
        runTest2 {
            every { cwaSettings.lastSuppressRootInfoVersionCode } returns flowOf(10)
            every { BuildConfigWrap.VERSION_CODE } returns 10L

            coEvery { rootBeer.isRooted } returns true

            with(createInstance(this)) {
                shouldShowRootInfo() shouldBe false
                every { cwaSettings.lastSuppressRootInfoVersionCode } returns flowOf(11)
                shouldShowRootInfo() shouldBe false
            }

            coVerify {
                rootBeer wasNot called
            }
        }

    @Test
    fun `updates lastSuppressRootInfoVersionCode with current version code if suppress is true`() = runTest2 {
        val versionCode = 123L
        every { BuildConfigWrap.VERSION_CODE } returns versionCode
        every { cwaSettings.lastSuppressRootInfoVersionCode } returns flowOf(versionCode)

        createInstance(this).suppressRootInfoForCurrentVersion(suppress = true)

        cwaSettings.lastSuppressRootInfoVersionCode.first() shouldBe versionCode

        verify {
            cwaSettings.lastSuppressRootInfoVersionCode
        }

        coVerify(exactly = 1) {
            cwaSettings.updateLastSuppressRootInfoVersionCode(versionCode)
        }
    }

    @Test
    fun `updates lastSuppressRootInfoVersionCode with default version code if suppress is false`() = runTest2 {
        val versionCode = 123L
        every { BuildConfigWrap.VERSION_CODE } returns versionCode

        createInstance(this).suppressRootInfoForCurrentVersion(suppress = false)

        cwaSettings.lastSuppressRootInfoVersionCode.first() shouldBe 0

        verify {
            cwaSettings.lastSuppressRootInfoVersionCode
        }

        coVerify(exactly = 1) {
            cwaSettings.updateLastSuppressRootInfoVersionCode(0L)
        }
    }
}
