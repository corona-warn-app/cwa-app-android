package de.rki.coronawarnapp.util.ui

import android.annotation.SuppressLint
import androidx.core.view.children
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.internal.ToolbarUtils

@SuppressLint("RestrictedApi")
fun MaterialToolbar.addMenuId(id: Int) {
    ToolbarUtils.getActionMenuView(this)?.children?.forEach {
        if (it.javaClass.name == "androidx.appcompat.widget.ActionMenuPresenter\$OverflowMenuButton") {
            it.id = id
        }
    }
}

@SuppressLint("RestrictedApi")
fun MaterialToolbar.addNavigationIconButtonId(id: Int) {
    ToolbarUtils.getNavigationIconButton(this)?.id = id
}

@SuppressLint("RestrictedApi")
fun MaterialToolbar.addTitleId(id: Int) {
    ToolbarUtils.getTitleTextView(this)?.id = id
}

@SuppressLint("RestrictedApi")
fun MaterialToolbar.addSubtitleId(id: Int) {
    ToolbarUtils.getSubtitleTextView(this)?.id = id
}
