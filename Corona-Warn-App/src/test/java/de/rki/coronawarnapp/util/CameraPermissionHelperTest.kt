package de.rki.coronawarnapp.util

import android.content.Context
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.CameraPermissionHelper.hasCameraPermission
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Test

class CameraPermissionHelperTest {

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication)
    }

    /**
     * Test call order is correct.
     */
    @Test
    fun hasCameraPermissionTest() {
        every { context.checkPermission(any(), any(), any()) } returns 0

        val result = hasCameraPermission(context)
        MatcherAssert.assertThat(result, CoreMatchers.`is`(true))
    }

    @Test
    fun hasNotCameraPermissionTest() {
        every { context.checkPermission(any(), any(), any()) } returns 1

        val result = hasCameraPermission(context)
        MatcherAssert.assertThat(result, CoreMatchers.`is`(false))
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
