package de.rki.coronawarnapp.util.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import timber.log.Timber

val View.hostLifecycle: Lifecycle?
    get() = try {
        findFragment<Fragment>().viewLifecycleOwner.lifecycle
    } catch (e: Exception) {
        Timber.v("Couldn't find viewLifecycleOwner for %s", this)
        null
    }

/**
 * Allows your view to receive callbacks from the host Fragment's lifecycle
 * Your callback is invoked when the owning Fragment/Activity receives the specified event state.
 *
 *  @param callback returns true if it should be consumed (one-time callback), or false if it was to stay registered.
 *
 * @return true if the callback has been added. Otherwise returns false,
 * i.e. if the view doesn't have a viewLifecycleOwner due to not being attached.
 */
fun View.addLifecycleEventCallback(
    type: Lifecycle.Event,
    callback: () -> Boolean
): Boolean {
    val hostLifecycle = hostLifecycle ?: return false

    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(owner: LifecycleOwner, event: Lifecycle.Event) {
            if (event != type) return
            Timber.v("%s triggered %s for %s", owner, event, this@addLifecycleEventCallback)
            val consumed = callback()
            if (consumed) hostLifecycle.removeObserver(this)
        }
    }

    hostLifecycle.addObserver(observer)
    return true
}

fun LottieAnimationView.setLottieAnimation(animation: Int?) {
    if (animation != null) {
        val context = context
        val type = context.resources.getResourceTypeName(animation)

        if (type == "drawable") {
            setImageDrawable(context.getDrawableCompat(animation))
        } else {
            setAnimation(animation)
            repeatCount = LottieDrawable.INFINITE
            repeatMode = LottieDrawable.RESTART
            playAnimation()
        }
    }
}

fun LottieAnimationView.setLottieAnimationColor(color: Int?) {
    if (color != null) {
        setColorFilter(color)
        addValueCallback(
            KeyPath("**"),
            LottieProperty.COLOR_FILTER
        ) { PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP) }
    }
}

fun ImageView.setCWAContentDescription(description: String?) {
    if (description == null) {
        Timber.w("Settings a null contentDescription on $id")
        return
    }
    contentDescription = formatSuffix(context, description, R.string.suffix_image)
}

fun View.setCWAContentDescription(description: String?) {
    if (description == null) {
        Timber.w("Settings a null contentDescription on $id")
        return
    }
    contentDescription = formatSuffix(context, description, R.string.suffix_button)
}

private fun formatSuffix(context: Context, prefix: String, @StringRes suffix: Int): String {
    return "$prefix ${context.getString(suffix)}"
}
