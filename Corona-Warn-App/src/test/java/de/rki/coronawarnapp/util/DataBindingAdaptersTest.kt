package de.rki.coronawarnapp.util

import android.content.Context
import android.widget.Switch
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Before
import org.junit.Test

class DataBindingAdaptersTest {

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun setChecked(status: Boolean?) {
        val switch = spyk(Switch(context))

        setChecked(switch, status)

        if (status != null) {
            verifySequence {
                switch.tag = IGNORE_CHANGE_TAG
                switch.isChecked = status
                switch.tag = null
            }
        } else {
            verify(exactly = 0) {
                switch.tag = IGNORE_CHANGE_TAG
                switch.isChecked = any()
                switch.tag = null
            }
        }
    }

    @Test
    fun setCheckedTrue() {
        setChecked(true)
    }

    @Test
    fun setCheckedFalse() {
        setChecked(false)
    }

    @Test
    fun setCheckedNull() {
        setChecked(false)
    }
}