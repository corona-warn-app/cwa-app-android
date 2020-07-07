package de.rki.coronawarnapp.util

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.Switch
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import de.rki.coronawarnapp.CoronaWarnApplication

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
        val appContext = CoronaWarnApplication.getAppContext()
        val type = appContext.resources.getResourceTypeName(animation)

        if (type == DRAWABLE_TYPE) {
            view.setImageDrawable(appContext.getDrawable(animation))
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
