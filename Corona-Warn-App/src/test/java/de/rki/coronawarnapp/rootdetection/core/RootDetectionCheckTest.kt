package de.rki.coronawarnapp.rootdetection.core

import com.scottyab.rootbeer.RootBeer
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.preferences.mockFlowPreference

class RootDetectionCheckTest : BaseTest() {

    @MockK private lateinit var rootBeer: RootBeer
    @MockK private lateinit var cwaSettings: CWASettings

    private val lastSuppressRootInfoVersionCode = mockFlowPreference(0L)

    private val testDispatcherProvider = TestDispatcherProvider()

    private fun createInstance() = RootDetectionCheck(
        rootBeer = rootBeer,
        dispatcherProvider = testDispatcherProvider,
        cwaSettings = cwaSettings
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_CODE } returns 0L

        every { cwaSettings.lastSuppressRootInfoVersionCode } returns lastSuppressRootInfoVersionCode
        lastSuppressRootInfoVersionCode.update { 0 }
    }

    @Test
    fun `device is rooted`() = runTest {
        every { rootBeer.isRooted } returns true
        createInstance().isRooted() shouldBe true
        verify { rootBeer.isRooted }
    }

    @Test
    fun `device is not rooted`() = runTest {
        every { rootBeer.isRooted } returns false
        createInstance().isRooted() shouldBe false
        verify { rootBeer.isRooted }
    }

    @Test
    fun `fallback to false in case of an error`() = runTest {
        every { rootBeer.isRooted } throws Exception("Test error")
        createInstance().isRooted() shouldBe false
        verify { rootBeer.isRooted }
    }

    @Test
    fun `device is rooted and current version is greater than last suppress root info version code`() =
        runTest {
            lastSuppressRootInfoVersionCode.update { 9L }
            every { BuildConfigWrap.VERSION_CODE } returns 10L

            coEvery { rootBeer.isRooted } returns true

            createInstance().shouldShowRootInfo() shouldBe true

            coVerify {
                rootBeer.isRooted
            }
        }

    @Test
    fun `device is not rooted and current version is greater than last suppress root info version code`() =
        runTest {
            lastSuppressRootInfoVersionCode.update { 9L }
            every { BuildConfigWrap.VERSION_CODE } returns 10L

            coEvery { rootBeer.isRooted } returns false

            createInstance().shouldShowRootInfo() shouldBe false

            coVerify {
                rootBeer.isRooted
            }
        }

    @Test
    fun `device is rooted but current version is less than or equal to last suppress root info version code`() =
        runTest {
            lastSuppressRootInfoVersionCode.update { 10 }
            every { BuildConfigWrap.VERSION_CODE } returns 10L

            coEvery { rootBeer.isRooted } returns true

            with(createInstance()) {
                shouldShowRootInfo() shouldBe false
                lastSuppressRootInfoVersionCode.update { 11 }
                shouldShowRootInfo() shouldBe false
            }

            coVerify {
                rootBeer wasNot called
            }
        }

    @Test
    fun `updates lastSuppressRootInfoVersionCode with current version code if suppress is true`() {
        val versionCode = 123L
        every { BuildConfigWrap.VERSION_CODE } returns versionCode

        createInstance().suppressRootInfoForCurrentVersion(suppress = true)

        lastSuppressRootInfoVersionCode.value shouldBe versionCode

        verify {
            cwaSettings.lastSuppressRootInfoVersionCode
        }
    }

    @Test
    fun `updates lastSuppressRootInfoVersionCode with default version code if suppress is false`() {
        val versionCode = 123L
        every { BuildConfigWrap.VERSION_CODE } returns versionCode

        lastSuppressRootInfoVersionCode.update { 456 }

        createInstance().suppressRootInfoForCurrentVersion(suppress = false)

        lastSuppressRootInfoVersionCode.value shouldBe 0

        verify {
            cwaSettings.lastSuppressRootInfoVersionCode
        }
    }
}
