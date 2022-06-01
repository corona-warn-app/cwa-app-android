package de.rki.coronawarnapp.dccreissuance.ui.consent

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.DccReissuanceCertificateCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class DccReissuanceCertificateCard(parent: ViewGroup) :
    DccReissuanceAdapter.ItemVH<DccReissuanceCertificateCard.Item, DccReissuanceCertificateCardBinding>(
        layoutRes = R.layout.dcc_reissuance_certificate_card,
        parent = parent
    ) {
    override val viewBinding: Lazy<DccReissuanceCertificateCardBinding> = lazy {
        DccReissuanceCertificateCardBinding.bind(itemView)
    }

    override val onBindData: DccReissuanceCertificateCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        when (val certificate = curItem.certificate) {
            is VaccinationDccV1 -> {
                setCertificate(
                    certificate.getIcon(),
                    vaccinationHeader,
                    certificate.getBodyText(),
                    item.isExpired,
                )
            }
            is RecoveryDccV1 -> {
                setCertificate(
                    RecoveryCertificate.icon,
                    recoveryHeader,
                    certificate.getBodyText(),
                    item.isExpired,
                )
            }
            is TestDccV1 -> {
                setCertificate(
                    TestCertificate.icon,
                    testHeader,
                    certificate.getBodyText(),
                    item.isExpired,
                )
            }
        }
    }

    private fun VaccinationDccV1.getBodyText(): String {
        val fullName = nameData.fullName
        val vaccinationDosesInfo = context.getString(
            R.string.vaccination_certificate_doses,
            vaccination.doseNumber,
            vaccination.totalSeriesOfDoses
        )
        val certificateDate = context.getString(
            R.string.vaccination_certificate_vaccinated_on,
            vaccination.vaccinatedOn?.toShortDayFormat()
                ?: vaccination.dt
        )
        return "$fullName\n$vaccinationDosesInfo\n$certificateDate"
    }

    private fun VaccinationDccV1.getIcon() = when {
        isSeriesCompletingShot -> VaccinationCertificate.iconComplete
        else -> VaccinationCertificate.iconIncomplete
    }

    private fun RecoveryDccV1.getBodyText(): String {
        val fullName = nameData.fullName
        val info = context.getString(
            R.string.recovery_certificate_sample_collection,
            recovery.testedPositiveOn?.toShortDayFormat()
                ?: recovery.fr
        )
        return "$fullName\n$info"
    }

    private fun TestDccV1.getBodyText(): String {
        val fullName = nameData.fullName
        var testType = ""
        when {
            // PCR Test
            isPCRTestCertificate -> R.string.test_certificate_pcr_test_type
            // RAT Test
            isRapidAntigenTestCertificate -> R.string.test_certificate_rapid_test_type
            else -> null
        }?.let {
            testType = context.getString(it)
        }

        val info = context.getString(
            R.string.test_certificate_sampled_on,
            test.sampleCollectedAt?.toUserTimeZone()?.toShortDayFormat()
                ?: test.sc
        )
        return "$fullName\n$testType\n$info"
    }

    private fun DccReissuanceCertificateCardBinding.setExpiredIcon() {
        dccReissuanceCertificateIcon.setImageDrawable(
            AppCompatResources.getDrawable(context, expiredIcon)
        )
        dccReissuanceCertificateBg.setImageDrawable(
            AppCompatResources.getDrawable(context, expiredBackground)
        )
    }

    private fun DccReissuanceCertificateCardBinding.setIcon(
        @DrawableRes iconRes: Int
    ) {
        dccReissuanceCertificateIcon.setImageDrawable(
            AppCompatResources.getDrawable(context, iconRes)
        )
    }

    private fun DccReissuanceCertificateCardBinding.setHeader(
        @StringRes text: Int
    ) {
        dccReissuanceHeader.text = context.getString(text)
    }

    private fun DccReissuanceCertificateCardBinding.setBody(
        text: String
    ) {
        dccReissuanceBody.text = text
    }

    private fun DccReissuanceCertificateCardBinding.setCertificate(
        @DrawableRes icon: Int,
        @StringRes header: Int,
        body: String,
        isExpired: Boolean,
    ) {
        if (isExpired) {
            setExpiredIcon()
        } else {
            setIcon(icon)
        }

        setHeader(header)
        setBody(body)
    }

    companion object {
        private const val expiredIcon = R.drawable.ic_certificate_invalid
        private const val expiredBackground = R.drawable.bg_certificate_grey
        private const val vaccinationHeader = R.string.vaccination_certificate_name
        private const val recoveryHeader = R.string.recovery_certificate_name
        private const val testHeader = R.string.test_certificate_name
    }

    data class Item(
        val certificate: DccV1.MetaData,
        val isExpired: Boolean = false,
    ) : DccReissuanceItem, HasPayloadDiffer {
        override val stableId = certificate.hashCode().toLong()
    }
}
