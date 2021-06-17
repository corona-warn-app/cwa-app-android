package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewAdapter
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.CovidTestErrorCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class CovidTestCertificatePendingCard(parent: ViewGroup) :
    PersonOverviewAdapter.PersonOverviewItemVH<CovidTestCertificatePendingCard.Item, CovidTestErrorCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {
    override val viewBinding = lazy {
        CovidTestErrorCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: CovidTestErrorCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        testTime.text = context.getString(
            R.string.test_certificate_time,
            curItem.certificate.registeredAt.toDayFormat(),
            curItem.certificate.registeredAt.toShortTimeFormat()
        )

        retryButton.setOnClickListener {
            item.onRetryAction(item)
        }

        if (curItem.certificate.isUpdatingData) {
            refreshStatus.isGone = false
            progressBar.show()
            retryButton.isInvisible = true
            deleteButton.isInvisible = true
            body.text = context.getString(R.string.test_certificate_error_label_refreshing)
        } else {
            refreshStatus.isGone = true
            progressBar.hide()
            retryButton.isInvisible = false
            deleteButton.isInvisible = false
            body.text = context.getString(R.string.test_certificate_error_label)
        }
        deleteButton.setOnClickListener { item.onDeleteAction(item) }
    }

    data class Item(
        val certificate: TestCertificate,
        val onRetryAction: (Item) -> Unit,
        val onDeleteAction: (Item) -> Unit
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = certificate.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
