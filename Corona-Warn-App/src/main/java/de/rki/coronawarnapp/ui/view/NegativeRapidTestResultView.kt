package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.databinding.PersonalRapidTestResultNegativeBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone

/**
 * The [NegativeRapidTestResultView] Displays the appropriate test result.
 */
class NegativeRapidTestResultView @JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: PersonalRapidTestResultNegativeBinding

    init {
        inflate(context, R.layout.personal_rapid_test_result_negative, this)
        binding = PersonalRapidTestResultNegativeBinding.bind(this)
    }

    fun setTestResultSection(coronaTest: RACoronaTest?) {
        binding.apply {
            timeInfo.text = formatTestResultTimestampText(coronaTest)
            patientInfo.text = formatPatientInfo(coronaTest).also {
                if (it.isBlank()) patientInfo.isVisible = false
            }
        }
    }

    private fun formatPatientInfo(test: RACoronaTest?) : SpannedString {
        val patientName = context.getString(
            R.string.submission_test_result_antigen_patient_name_placeholder,
            test?.firstName ?: "",
            test?.lastName ?: ""
        )

        return buildSpannedString {
            bold {
                if (patientName.isNotBlank()) append(patientName)
            }
            test?.dateOfBirth?.let {
                val birthDate = context.getString(
                    R.string.submission_test_result_antigen_patient_birth_date_placeholder,
                    it.toDayFormat()
                )
                if (this.isNotBlank()) append(", ")
                append(birthDate)
            }
        }
    }

    private fun formatTestResultTimestampText(test: RACoronaTest?): String {
        val localTime = test?.testTakenAt?.toUserTimeZone() ?: return ""
        return context.getString(
            R.string.coronatest_negative_antigen_result_time_date_placeholder,
            localTime.toDayFormat(),
            localTime.toShortTimeFormat()
        )
    }
}
