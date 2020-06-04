package de.rki.coronawarnapp.util

import android.util.Log
import de.rki.coronawarnapp.CoronaWarnApplication
import java.util.Properties

class PropertyLoader {
    companion object {
        private const val PIN_PROPERTIES_FILE_NAME = "pins.properties"
        private const val PIN_FILE_DELIMITER = ","
        private const val DISTRIBUTION_PIN_PROPERTY_NAME = "DISTRIBUTION_PINS"
        private const val SUBMISSION_PINS_PROPERTY_NAME = "SUBMISSION_PINS"
        private const val VERIFICATION_PINS_PROPERTY_NAME = "VERIFICATION_PINS"
    }

    fun getDistributionPins() = getCertificatePins(DISTRIBUTION_PIN_PROPERTY_NAME)
    fun getSubmissionPins() = getCertificatePins(SUBMISSION_PINS_PROPERTY_NAME)
    fun getVerificationPins() = getCertificatePins(VERIFICATION_PINS_PROPERTY_NAME)

    private fun getCertificatePins(key: String): Array<String> = Properties().run {
        this.load(CoronaWarnApplication.getAppContext().assets.open(PIN_PROPERTIES_FILE_NAME))
        this.getProperty(key)
            .split(PIN_FILE_DELIMITER)
            .filter { it.isNotEmpty() }
            .also { Log.v(key, it.toString()) }
            .toTypedArray()
    }
}
