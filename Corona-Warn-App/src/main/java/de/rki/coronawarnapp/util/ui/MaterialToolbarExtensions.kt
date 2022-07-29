package de.rki.coronawarnapp.util.ui

import android.annotation.SuppressLint
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.internal.ToolbarUtils

@SuppressLint("RestrictedApi")
fun MaterialToolbar.addMenuId(id: Int) {
    ToolbarUtils.getActionMenuView(this)?.id = id
}

@SuppressLint("RestrictedApi")
fun MaterialToolbar.addNavigationIconButtonId(id: Int) {
    ToolbarUtils.getNavigationIconButton(this)?.id = id
}
