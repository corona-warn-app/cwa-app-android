package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.TestCertificateCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.displayExpirationState
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class TestCertificateCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<TestCertificateCard.Item, TestCertificateCardBinding>(
        layoutRes = R.layout.test_certificate_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<TestCertificateCardBinding> = lazy {
        TestCertificateCardBinding.bind(itemView)
    }
    override val onBindData: TestCertificateCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        val certificate = curItem.certificate
        root.setOnClickListener { curItem.onClick() }
        certificateDate.text = context.getString(
            R.string.test_certificate_sampled_on,
            certificate.sampleCollectedAt?.toUserTimeZone()?.toShortDayFormat() ?: certificate.rawCertificate.test.sc
        )

        when {
            // PCR Test
            certificate.isPCRTestCertificate -> R.string.test_certificate_pcr_test_type
            // RAT Test
            else -> R.string.test_certificate_rapid_test_type
        }.also { testCertificateType.setText(it) }

        val bookmarkIcon =
            if (curItem.certificate.isDisplayValid) curItem.colorShade.bookmarkIcon else R.drawable.ic_bookmark
        currentCertificateGroup.isVisible = curItem.isCurrentCertificate
        bookmark.setImageResource(bookmarkIcon)
        val color = when {
            curItem.certificate.isDisplayValid -> curItem.colorShade
            else -> PersonColorShade.COLOR_INVALID
        }

        when {
            curItem.certificate.isDisplayValid -> R.drawable.ic_test_certificate
            else -> R.drawable.ic_certificate_invalid
        }.also { certificateIcon.setImageResource(it) }

        val background = when {
            curItem.isCurrentCertificate -> color.currentCertificateBg
            else -> color.defaultCertificateBg
        }
        certificateBg.setImageResource(background)

        notificationBadge.isVisible = curItem.certificate.hasNotificationBadge

        certificateExpiration.displayExpirationState(curItem.certificate)

        startValidationCheckButton.apply {
            defaultButton.isEnabled = certificate.isNotBlocked
            isEnabled = certificate.isNotBlocked
            isLoading = curItem.isLoading
            defaultButton.setOnClickListener {
                curItem.validateCertificate(certificate.containerId)
            }
        }
    }

    data class Item(
        val certificate: TestCertificate,
        val isCurrentCertificate: Boolean,
        val colorShade: PersonColorShade,
        val isLoading: Boolean = false,
        val onClick: () -> Unit,
        val validateCertificate: (CertificateContainerId) -> Unit,
    ) : CertificateItem, HasPayloadDiffer {
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
