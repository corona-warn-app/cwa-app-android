package de.rki.coronawarnapp.util.ui.bindingadapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.internal.ToolbarUtils
import de.rki.coronawarnapp.R
import timber.log.Timber

private fun formatSuffix(context: Context, prefix: String, @StringRes suffix: Int): String {
    return "$prefix ${context.getString(suffix)}"
}

@BindingAdapter("cwaContentDescription")
fun ImageView.setCWAContentDescription(description: String?) {
    if (description == null) {
        Timber.w("Settings a null contentDescription on $id")
        return
    }
    contentDescription = formatSuffix(context, description, R.string.suffix_image)
}

@BindingAdapter("cwaContentDescription")
fun View.setCWAContentDescription(description: String?) {
    if (description == null) {
        Timber.w("Settings a null contentDescription on $id")
        return
    }
    contentDescription = formatSuffix(context, description, R.string.suffix_button)
}

@SuppressLint("RestrictedApi")
@BindingAdapter("navigationButtonId")
fun MaterialToolbar.setNavigationButtonId(id: Int) {
    ToolbarUtils.getNavigationIconButton(this)?.id =  id
}

@SuppressLint("RestrictedApi")
@BindingAdapter("titleTextViewId")
fun MaterialToolbar.settitleTextViewId(id: Int) {
    ToolbarUtils.getTitleTextView(this)?.id =  id
}
