package de.rki.coronawarnapp.ui.main.home.rampdown

import de.rki.coronawarnapp.ccl.rampdown.calculation.RampDownCalculation
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RampDownDataProvider @Inject constructor(
    format: CclTextFormatter,
    rampDownCalculation: RampDownCalculation
) {

    val rampDownNotice = flow {
        val notice = try {
            val rampDownOutput = rampDownCalculation.getStatusTabNotice()
            RampDownNotice(
                visible = rampDownOutput.visible,
                title = format(rampDownOutput.titleText),
                subtitle = format(rampDownOutput.subtitleText),
                description = format(rampDownOutput.longText),
                faqUrl = format(rampDownOutput.faqAnchor),
            )
        } catch (e: Exception) {
            Timber.d(e, "RampDown failed")
            null
        }
        emit(notice)
    }
}
