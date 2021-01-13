package de.rki.coronawarnapp.util

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.widget.Switch
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Before
import org.junit.Test

class DataBindingAdaptersTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var drawable: Drawable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(ContextExtensions)
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

    private fun setAnimation(animation: Int?) {
        val animationView = mockk<LottieAnimationView>(relaxUnitFun = true).apply {
            every { context } returns mockk<Context>().apply {
                every { resources } returns mockk<Resources>().apply {
                    every { getResourceTypeName(any()) } returns "raw"
                }
            }
        }

        setAnimation(animationView, animation)

        verify(exactly = 0) {
            animationView.setImageDrawable(any())
        }

        if (animation != null) {
            verifySequence {
                animationView.context
                animationView.setAnimation(animation)
                animationView.repeatCount = LottieDrawable.INFINITE
                animationView.repeatMode = LottieDrawable.RESTART
                animationView.playAnimation()
            }
        } else {
            verify(exactly = 0) {
                animationView.setAnimation(any<Int>())
                animationView.repeatCount = any()
                animationView.repeatMode = any()
                animationView.playAnimation()
            }
        }
    }

    @Test
    fun setAnimationIcon() {
        setAnimation(R.raw.ic_settings_tracing_animated)
    }

    @Test
    fun setAnimationNull() {
        setAnimation(null)
    }

    private fun setDrawable(drawableId: Int?) {
        val animationView = mockk<LottieAnimationView>(relaxUnitFun = true).apply {
            every { context } returns mockk<Context>().apply {
                every { resources } returns mockk<Resources>().apply {
                    every { getResourceTypeName(any()) } returns DRAWABLE_TYPE
                }
                every { getDrawableCompat(any()) } returns this@DataBindingAdaptersTest.drawable
            }
        }

        setAnimation(animationView, drawableId)

        verify(exactly = 0) {
            animationView.setAnimation(any<Int>())
            animationView.repeatCount = any()
            animationView.repeatMode = any()
            animationView.playAnimation()
        }

        if (drawableId != null) {
            verifySequence {
                animationView.context
                animationView.setImageDrawable(any())
            }
        } else {
            verify(exactly = 0) {
                animationView.setImageDrawable(any())
            }
        }
    }

    @Test
    fun setDrawableIcon() {
        setDrawable(R.drawable.ic_settings_tracing_inactive)
    }

    @Test
    fun setDrawableNull() {
        setDrawable(null)
    }

    private fun setAnimationColor(color: Int?) {
        val animationView = mockk<LottieAnimationView>(relaxUnitFun = true)

        setAnimationColor(animationView, color)

        if (color != null) {
            verify {
                animationView.setColorFilter(color)
            }
        } else {
            verify(exactly = 0) {
                animationView.setColorFilter(any<Int>())
            }
        }
    }

    @Test
    fun setAnimationColorColor() {
        setAnimationColor(R.color.colorTextSemanticRed)
    }

    @Test
    fun setAnimationColorNull() {
        setAnimationColor(null)
    }
}
