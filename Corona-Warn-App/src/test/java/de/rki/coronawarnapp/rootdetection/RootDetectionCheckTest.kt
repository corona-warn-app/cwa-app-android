package de.rki.coronawarnapp.rootdetection

import com.scottyab.rootbeer.RootBeer
import de.rki.coronawarnapp.rootdetection.core.RootDetectionCheck
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

class RootDetectionCheckTest : BaseTest() {

    @MockK private lateinit var rootBeer: RootBeer

    private val testDispatcherProvider = TestDispatcherProvider()

    private fun createInstance() = RootDetectionCheck(
        rootBeer = rootBeer,
        dispatcherProvider = testDispatcherProvider
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `device is rooted`() = runBlockingTest {
        every { rootBeer.isRooted } returns true
        createInstance().isRooted() shouldBe true
        verify { rootBeer.isRooted }
    }

    @Test
    fun `device is not rooted`() = runBlockingTest {
        every { rootBeer.isRooted } returns false
        createInstance().isRooted() shouldBe false
        verify { rootBeer.isRooted }
    }

    @Test
    fun `fallback to false in case of an error`() = runBlockingTest {
        every { rootBeer.isRooted } throws Exception("Test error")
        createInstance().isRooted() shouldBe false
        verify { rootBeer.isRooted }
    }
}
