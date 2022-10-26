package de.rki.coronawarnapp.reyclebin.ui.common

import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.RecyclerBinCertificateItemBinding
import de.rki.coronawarnapp.reyclebin.common.Recyclable
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun RecyclerBinCertificateItemBinding.addDeletionInfoIfExists(item: Recyclable) {
    Timber.tag(TAG).v("addDeletionInfo(item=%s)", item)

    certificateDeletionDateInfo.isVisible = item.recycledAt != null
    item.recycledAt?.let { recycledAt ->
        val deletionDate = recycledAt.plus(Recyclable.RETENTION_DAYS).toLocalDateTimeUserTz()
        certificateDeletionDateInfo.text = with(deletionDate) {
            root.context.getString(
                R.string.recycle_bin_item_deletion_date_info,
                format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            ).also { Timber.tag(TAG).v("Deletion date info: %s", it) }
        }
    }
}

private const val TAG = "RecyclerBinCertificateItemBindingExtension"
