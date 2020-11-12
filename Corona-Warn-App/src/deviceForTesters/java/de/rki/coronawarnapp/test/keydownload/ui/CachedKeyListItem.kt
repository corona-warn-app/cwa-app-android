package de.rki.coronawarnapp.test.keydownload.ui

import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.util.lists.HasStableId

data class CachedKeyListItem(
    val info: CachedKeyInfo,
    val fileSize: Long
) : HasStableId {
    override val stableId: Long
        get() = info.id.hashCode().toLong()
}
