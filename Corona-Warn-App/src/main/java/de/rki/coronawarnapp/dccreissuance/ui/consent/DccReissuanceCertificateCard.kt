package de.rki.coronawarnapp.dccreissuance.ui.consent

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.databinding.DccReissuanceCertificateCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone

class DccReissuanceCertificateCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val itemView: View =
        LayoutInflater.from(context).inflate(R.layout.dcc_reissuance_certificate_card, this, true)

    private val viewBinding by lazy {
        DccReissuanceCertificateCardBinding.bind(itemView)
    }

    var certificate: DccV1.MetaData? = null
        set(value) {
            val fullName = value?.nameData?.fullName
            when (value) {
                is VaccinationDccV1 -> {
                    val vaccinationDosesInfo = context.getString(
                        R.string.vaccination_certificate_doses,
                        value.vaccination.doseNumber,
                        value.vaccination.totalSeriesOfDoses
                    )

                    val certificateDate = context.getString(
                        R.string.vaccination_certificate_vaccinated_on,
                        value.vaccination.vaccinatedOn?.toShortDayFormat() ?: value.vaccination.dt
                    )
                    setVaccination("$fullName\n$vaccinationDosesInfo\n$certificateDate")
                }
                is RecoveryDccV1 -> {
                    val info = context.getString(
                        R.string.recovery_certificate_sample_collection,
                        value.recovery.testedPositiveOn?.toShortDayFormat() ?: value.recovery.fr
                    )
                    setRecovery("$fullName\n$info")
                }
                is TestDccV1 -> {
                    var testType = ""
                    when {
                        // PCR Test
                        value.isPCRTestCertificate -> R.string.test_certificate_pcr_test_type
                        // RAT Test
                        value.isRapidAntigenTestCertificate -> R.string.test_certificate_rapid_test_type
                        else -> null
                    }?.let {
                        testType = context.getString(it)
                    }

                    val info = context.getString(
                        R.string.test_certificate_sampled_on,
                        value.test.sampleCollectedAt?.toUserTimeZone()?.toShortDayFormat() ?: value.test.sc
                    )
                    setTest("$fullName\n$testType\n$info")
                }
            }
            field = value
        }

    private fun setVaccination(body: String) {
        viewBinding.dccReissuanceCertificateIcon.setImageDrawable(
            AppCompatResources.getDrawable(context, vaccinationIcon)
        )
        viewBinding.dccReissuanceHeader.text = context.getString(R.string.vaccination_certificate_name)
        viewBinding.dccReissuanceBody.text = body
    }

    private fun setRecovery(body: String) {
        viewBinding.dccReissuanceCertificateIcon.setImageDrawable(
            AppCompatResources.getDrawable(context, recoveryIcon)
        )
        viewBinding.dccReissuanceHeader.text = context.getString(R.string.recovery_certificate_name)
        viewBinding.dccReissuanceBody.text = body
    }

    private fun setTest(body: String) {
        viewBinding.dccReissuanceCertificateIcon.setImageDrawable(
            AppCompatResources.getDrawable(context, testIcon)
        )
        viewBinding.dccReissuanceHeader.text = context.getString(R.string.test_certificate_name)
        viewBinding.dccReissuanceBody.text = body
    }

    companion object {
        private const val vaccinationIcon = R.drawable.ic_vaccination_immune
        private const val recoveryIcon = R.drawable.ic_recovery_certificate
        private const val testIcon = R.drawable.ic_test_certificate
    }
}
