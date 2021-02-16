package de.rki.coronawarnapp.datadonation.analytics.ui.input

import de.rki.coronawarnapp.util.ui.LazyString

data class UserInfoItem(
    val label: LazyString,
    val isSelected: Boolean,
    val data: Any
)
