package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.ui.BaseFragment
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * CallHelper test.
 */
class CallHelperTest {

    /**
     * Test activity called.
     */
    @Test
    fun testCall() {
        val fragment = mockk<BaseFragment>()
        every { fragment.startActivity(any()) } just Runs
        CallHelper.call(fragment, "+77777777777")
        verify(exactly = 1) { fragment.startActivity(any()) }
    }
}
