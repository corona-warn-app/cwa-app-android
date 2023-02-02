package de.rki.coronawarnapp.ui.main.home.rampdown

import dagger.Reusable
import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.ccl.rampdown.calculation.RampDownCalculation
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RampDownDataProvider @Inject constructor(
    format: CclTextFormatter,
    rampDownCalculation: RampDownCalculation,
    cclConfigurationRepository: CclConfigurationRepository,
) {

    val rampDownNotice = cclConfigurationRepository
        .cclConfigurations
        .distinctUntilChanged()
        .map {
            Timber.d("calculating rampDownNotice ...")
            try {
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
        }
}
