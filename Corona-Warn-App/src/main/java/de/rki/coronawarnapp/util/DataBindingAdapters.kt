package de.rki.coronawarnapp.util

import android.widget.Switch
import androidx.databinding.BindingAdapter

const val IGNORE_CHANGE_TAG = "ignore"

@BindingAdapter("checked")
fun setChecked(switch: Switch, status: Boolean?) {
    if (status != null) {
        switch.tag = IGNORE_CHANGE_TAG
        switch.isChecked = status
        switch.tag = null
    }
}
