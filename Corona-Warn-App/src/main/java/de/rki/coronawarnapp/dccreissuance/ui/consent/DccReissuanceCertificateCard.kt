package de.rki.coronawarnapp.dccreissuance.ui.consent

import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
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
        val fullName = curItem.certificate.nameData.fullName
        when (curItem.certificate) {
            is VaccinationDccV1 -> {
                val vaccinationDosesInfo = context.getString(
                    R.string.vaccination_certificate_doses,
                    curItem.certificate.vaccination.doseNumber,
                    curItem.certificate.vaccination.totalSeriesOfDoses
                )

                val certificateDate = context.getString(
                    R.string.vaccination_certificate_vaccinated_on,
                    curItem.certificate.vaccination.vaccinatedOn?.toShortDayFormat()
                        ?: curItem.certificate.vaccination.dt
                )
                setVaccination("$fullName\n$vaccinationDosesInfo\n$certificateDate", item.isExpired)
            }
            is RecoveryDccV1 -> {
                val info = context.getString(
                    R.string.recovery_certificate_sample_collection,
                    curItem.certificate.recovery.testedPositiveOn?.toShortDayFormat()
                        ?: curItem.certificate.recovery.fr
                )
                setRecovery("$fullName\n$info", item.isExpired)
            }
            is TestDccV1 -> {
                var testType = ""
                when {
                    // PCR Test
                    curItem.certificate.isPCRTestCertificate -> R.string.test_certificate_pcr_test_type
                    // RAT Test
                    curItem.certificate.isRapidAntigenTestCertificate -> R.string.test_certificate_rapid_test_type
                    else -> null
                }?.let {
                    testType = context.getString(it)
                }

                val info = context.getString(
                    R.string.test_certificate_sampled_on,
                    curItem.certificate.test.sampleCollectedAt?.toUserTimeZone()?.toShortDayFormat()
                        ?: curItem.certificate.test.sc
                )
                setTest("$fullName\n$testType\n$info", item.isExpired)
            }
        }
    }

    private fun DccReissuanceCertificateCardBinding.setExpired() {
        dccReissuanceCertificateIcon.setImageDrawable(
            AppCompatResources.getDrawable(context, expiredIcon)
        )
        dccReissuanceCertificateBg.setImageDrawable(
            AppCompatResources.getDrawable(context, expiredBackground)
        )
    }

    private fun DccReissuanceCertificateCardBinding.setVaccination(
        body: String,
        isExpired: Boolean
    ) {
        if (isExpired) {
            setExpired()
        } else {
            dccReissuanceCertificateIcon.setImageDrawable(
                AppCompatResources.getDrawable(context, vaccinationIcon)
            )
        }

        dccReissuanceHeader.text = context.getString(R.string.vaccination_certificate_name)
        dccReissuanceBody.text = body
    }

    private fun DccReissuanceCertificateCardBinding.setRecovery(
        body: String,
        isExpired: Boolean
    ) {
        if (isExpired) {
            setExpired()
        } else {
            dccReissuanceCertificateIcon.setImageDrawable(
                AppCompatResources.getDrawable(context, recoveryIcon)
            )
        }
        dccReissuanceHeader.text = context.getString(R.string.recovery_certificate_name)
        dccReissuanceBody.text = body
    }

    private fun DccReissuanceCertificateCardBinding.setTest(
        body: String,
        isExpired: Boolean
    ) {
        if (isExpired) {
            setExpired()
        } else {
            dccReissuanceCertificateIcon.setImageDrawable(
                AppCompatResources.getDrawable(context, testIcon)
            )
        }
        dccReissuanceHeader.text = context.getString(R.string.test_certificate_name)
        dccReissuanceBody.text = body
    }

    companion object {
        private const val vaccinationIcon = R.drawable.ic_vaccination_immune
        private const val recoveryIcon = R.drawable.ic_recovery_certificate
        private const val testIcon = R.drawable.ic_test_certificate
        private const val expiredIcon = R.drawable.ic_certificate_invalid
        private const val expiredBackground = R.drawable.bg_certificate_grey
    }

    data class Item(
        val certificate: DccV1.MetaData,
        val isExpired: Boolean = false,
    ) : DccReissuanceItem, HasPayloadDiffer {
        override val stableId = certificate.hashCode().toLong()
    }
}
