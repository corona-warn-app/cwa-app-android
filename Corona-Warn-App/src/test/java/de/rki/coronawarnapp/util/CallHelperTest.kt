package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.util.IndexHelper.convertToIndex
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

    @Test
    fun testCall() {
        val fragment = mockk<BaseFragment>()
        every { fragment.startActivity(any()) } just Runs
        CallHelper.call(fragment, "+77777777777")
        verify(exactly = 1) { fragment.startActivity(any()) }
    }
}
