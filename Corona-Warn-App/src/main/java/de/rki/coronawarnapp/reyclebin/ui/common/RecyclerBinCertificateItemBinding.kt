package de.rki.coronawarnapp.reyclebin.ui.common

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.RecyclerBinCertificateItemBinding
import de.rki.coronawarnapp.reyclebin.common.Recyclable
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import timber.log.Timber

fun RecyclerBinCertificateItemBinding.addDeletionInfo(item: Recyclable) {
    Timber.tag(TAG).v("addDeletionInfo(item=%s)", item)

    val recycledAt = item.recycledAt
    checkNotNull(recycledAt) { "recycledAt was not set" }

    val deletionDate = recycledAt.plus(Recyclable.RETENTION_DAYS)
    certificateDeletionDateInfo.text = with(deletionDate) {
        root.context.getString(
            R.string.recycle_bin_item_deletion_date_info,
            toShortDayFormat(),
            toShortTimeFormat()
        ).also { Timber.tag(TAG).v("Deletion date info: %s", it) }
    }
}

private const val TAG = "RecyclerBinCertificateItemBindingExtension"
