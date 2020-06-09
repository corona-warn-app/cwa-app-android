package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class FormatterHelperTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var drawable: Drawable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication.Companion)

        every { CoronaWarnApplication.getAppContext() } returns context
    }

    private fun formatVisibilityBase(bValue: Boolean, iResult: Int) {
        val result = formatVisibility(value = bValue)
        assertThat(result, `is`((iResult)))
    }

    private fun formatVisibilityIconBase(anyDrawable: Any?, iResult: Int) {
        val result = formatVisibilityIcon(drawable = anyDrawable)
        assertThat(result, `is`((iResult)))
    }

    private fun formatVisibilityInvertedBase(bValue: Boolean) {
        val result = formatVisibilityInverted(value = bValue)
        assertThat(result, `is`((formatVisibility(!bValue))))
    }

    private fun formatVisibilityTextBase(bValue: Boolean, sText: String?) {
        val result = formatVisibilityText(text = sText)
        assertThat(result, `is`((formatVisibility(bValue))))
    }

    private fun formatTextBase(bValue: Boolean?, iResult: Int) {
        every { context.getString(1) } returns "true string"
        every { context.getString(2) } returns "false string"

        val result = formatText(value = bValue, stringTrue = 1, stringFalse = 2)
        assertThat(result, `is`((CoronaWarnApplication.getAppContext().getString(iResult))))
    }

    private fun formatDrawableBase(bValue: Boolean) {
        every { context.getDrawable(1) } returns drawable
        every { context.getDrawable(2) } returns drawable

        val result = formatDrawable(value = bValue, drawableTrue = 1, drawableFalse = 2)
        assertThat(result, `is`((equalTo(drawable))))
    }

    private fun formatColorBase(bValue: Boolean, iColor: Int) {
        every { context.getColor(1) } returns 1
        every { context.getColor(2) } returns 2

        val result = formatColor(value = bValue, colorTrue = 1, colorFalse = 2)
        assertThat(result, `is`((context.getColor(iColor))))
    }

    @Test
    fun formatVisibility() {
        // Check visibility when value true
        formatVisibilityBase(bValue = true, iResult = View.VISIBLE)

        // Check visibility when value false
        formatVisibilityBase(bValue = false, iResult = View.GONE)
    }

    @Test
    fun formatVisibilityIcon() {
        // Check visibilityIcon when value not null
        formatVisibilityIconBase(anyDrawable = Any(), iResult = View.VISIBLE)

        // Check visibilityIcon when value null
        formatVisibilityIconBase(anyDrawable = null, iResult = View.GONE)
    }

    @Test
    fun formatVisibilityInverted() {
        // Check visibilityIcon when value true
        formatVisibilityInvertedBase(bValue = true)

        // Check visibilityIcon when value false
        formatVisibilityInvertedBase(bValue = false)
    }

    @Test
    fun formatVisibilityText() {
        // Check visibilityText when value true and text is not empty
        formatVisibilityTextBase(bValue = true, sText = "NOT_NULL_STRING")

        // Check visibilityText when value true and text is null
        formatVisibilityTextBase(bValue = false, sText = null)

        // Check visibilityText when value true and text is empty
        formatVisibilityTextBase(bValue = false, sText = "")
    }

    @Test
    fun formatText() {
        // Check  formatText when value true
        formatTextBase(bValue = true, iResult = 1)

        // Check  formatText when value false
        formatTextBase(bValue = false, iResult = 2)

        // Check  formatText when value false
        formatTextBase(bValue = null, iResult = 2)
    }

    @Test
    fun formatDrawable() {
        // Check formatDrawable when value true
        formatDrawableBase(bValue = true)

        // Check formatDrawable when value false
        formatDrawableBase(bValue = false)
    }

    @Test
    fun formatColorFalse() {
        // Check formatColor when value true
        formatColorBase(bValue = true, iColor = 1)

        // Check formatColor when value false
        formatColorBase(bValue = false, iColor = 2)
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
