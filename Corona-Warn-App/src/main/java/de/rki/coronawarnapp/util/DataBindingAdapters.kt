package de.rki.coronawarnapp.util

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.ImageView
import android.widget.Switch
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat

const val IGNORE_CHANGE_TAG = "ignore"
const val DRAWABLE_TYPE = "drawable"

@BindingAdapter("checked")
fun setChecked(switch: Switch, status: Boolean?) {
    if (status != null) {
        switch.tag = IGNORE_CHANGE_TAG
        switch.isChecked = status
        switch.tag = null
    }
}

@BindingAdapter("animation")
fun setAnimation(view: LottieAnimationView, animation: Int?) {
    if (animation != null) {
        val context = view.context
        val type = context.resources.getResourceTypeName(animation)

        if (type == DRAWABLE_TYPE) {
            view.setImageDrawable(context.getDrawableCompat(animation))
        } else {
            view.setAnimation(animation)
            view.repeatCount = LottieDrawable.INFINITE
            view.repeatMode = LottieDrawable.RESTART
            view.playAnimation()
        }
    }
}

@BindingAdapter("animation_tint")
fun setAnimationColor(view: LottieAnimationView, color: Int?) {
    if (color != null) {
        view.setColorFilter(color)
        view.addValueCallback(
            KeyPath("**"),
            LottieProperty.COLOR_FILTER
        ) { PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP) }
    }
}

@BindingAdapter("app:tint")
fun setTint(view: ImageView, color: Int) {
    ImageViewCompat.setImageTintList(view, ColorStateList.valueOf(color))
}
