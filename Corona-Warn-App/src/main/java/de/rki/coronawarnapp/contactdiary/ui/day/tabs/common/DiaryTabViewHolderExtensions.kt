package de.rki.coronawarnapp.contactdiary.ui.day.tabs.common

import android.view.View
import com.google.android.material.button.MaterialButtonToggleGroup

fun MaterialButtonToggleGroup.setOnCheckedChangeListener(listener: (checkedId: Int?) -> Unit) {
    clearOnButtonCheckedListeners()
    addOnButtonCheckedListener { group, checkedId, isChecked ->
        when {
            isChecked -> listener.invoke(checkedId)
            group.checkedButtonId == View.NO_ID -> listener.invoke(null)
        }
    }
}
