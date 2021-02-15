package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.common.SelectableDiaryItem
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toResolvingString

data class DiaryPersonListItem(
    override val item: ContactDiaryPerson,
    val personEncounter: ContactDiaryPersonEncounter?,
    override val onItemClick: (SelectableDiaryItem<ContactDiaryPerson>) -> Unit,
    val onDurationChanged: (DiaryPersonListItem, ContactDiaryPersonEncounter.DurationClassification?) -> Unit,
    val onWithMaskChanged: (DiaryPersonListItem, Boolean?) -> Unit,
    val onWasOutsideChanged: (DiaryPersonListItem, Boolean?) -> Unit,
    val onCircumstancesChanged: (DiaryPersonListItem, String) -> Unit,
    val onCircumstanceInfoClicked: () -> Unit
) : SelectableDiaryItem<ContactDiaryPerson>() {

    override val selected: Boolean
        get() = personEncounter != null

    override val contentDescription: LazyString
        get() = if (selected) {
            SELECTED_CONTENT_DESCRIPTION.toResolvingString(item.fullName)
        } else {
            UNSELECTED_CONTENT_DESCRIPTION.toResolvingString(item.fullName)
        }

    override val onClickDescription: LazyString
        get() = if (selected) {
            UNSELECTED_CONTENT_DESCRIPTION.toResolvingString(item.fullName)
        } else {
            SELECTED_CONTENT_DESCRIPTION.toResolvingString(item.fullName)
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

        other as DiaryPersonListItem

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

private const val SELECTED_CONTENT_DESCRIPTION = R.string.accessibility_person_selected
private const val UNSELECTED_CONTENT_DESCRIPTION = R.string.accessibility_person_unselected
private const val SELECT_ACTION_DESCRIPTION = R.string.accessibility_action_select
private const val DESELECT_ACTION_DESCRIPTION = R.string.accessibility_action_deselect
