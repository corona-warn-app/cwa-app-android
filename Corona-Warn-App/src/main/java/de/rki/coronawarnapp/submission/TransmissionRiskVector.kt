package de.rki.coronawarnapp.submission

class TransmissionRiskVector(private val values: IntArray) {

    companion object {

        private const val DEFAULT_TRANSMISSION_RISK_LEVEL = 1
    }

    fun getRiskValue(index: Int) =
        if (index < values.size) values[index] else DEFAULT_TRANSMISSION_RISK_LEVEL
}
