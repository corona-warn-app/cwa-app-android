package de.rki.coronawarnapp.util.ui.bindingadapters

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import de.rki.coronawarnapp.R

private fun formatSuffix(context: Context, prefix: String, @StringRes suffix: Int): String {
    return "$prefix ${context.getString(suffix)}"
}

@BindingAdapter("cwaContentDescription")
fun ImageView.setCWAContentDescription(description: String) {
    contentDescription = formatSuffix(context, description, R.string.suffix_image)
}

@BindingAdapter("cwaContentDescription")
fun View.setCWAContentDescription(description: String) {
    contentDescription = formatSuffix(context, description, R.string.suffix_button)
}
