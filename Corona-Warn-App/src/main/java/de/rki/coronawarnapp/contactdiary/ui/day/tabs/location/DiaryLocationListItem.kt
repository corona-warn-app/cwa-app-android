package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.common.SelectableDiaryItem
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import org.joda.time.Duration

data class DiaryLocationListItem(
    override val item: ContactDiaryLocation,
    val visit: ContactDiaryLocationVisit?,
    override val onItemClick: (SelectableDiaryItem<ContactDiaryLocation>) -> Unit,
    val onDurationChanged: (DiaryLocationListItem, Duration?) -> Unit,
    val onCircumstancesChanged: (DiaryLocationListItem, String) -> Unit,
    val onCircumStanceInfoClicked: () -> Unit
) : SelectableDiaryItem<ContactDiaryLocation>() {
    override val selected: Boolean
        get() = visit != null

    override val contentDescription: LazyString
        get() = if (selected) {
            SELECTED_CONTENT_DESCRIPTION.toResolvingString(item.locationName)
        } else {
            UNSELECTED_CONTENT_DESCRIPTION.toResolvingString(item.locationName)
        }
    override val onClickDescription: LazyString
        get() = if (selected) {
            UNSELECTED_CONTENT_DESCRIPTION.toResolvingString(item.locationName)
        } else {
            SELECTED_CONTENT_DESCRIPTION.toResolvingString(item.locationName)
        }
    override val clickLabel: Int
        @StringRes get() = if (selected) {
            DESELECT_ACTION_DESCRIPTION
        } else {
            SELECT_ACTION_DESCRIPTION
        }
    override val onClickLabel: Int
        @StringRes get() = if (selected) {
            SELECT_ACTION_DESCRIPTION
        } else {
            DESELECT_ACTION_DESCRIPTION
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiaryLocationListItem

        if (item != other.item) return false
        if (selected != other.selected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = item.hashCode()
        result = 31 * result + selected.hashCode()
        return result
    }
}

private const val SELECTED_CONTENT_DESCRIPTION = R.string.accessibility_location_selected
private const val UNSELECTED_CONTENT_DESCRIPTION = R.string.accessibility_location_unselected
private const val SELECT_ACTION_DESCRIPTION = R.string.accessibility_action_select
private const val DESELECT_ACTION_DESCRIPTION = R.string.accessibility_action_deselect
