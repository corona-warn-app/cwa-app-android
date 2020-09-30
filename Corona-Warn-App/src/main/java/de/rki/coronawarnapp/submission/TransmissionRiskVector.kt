package de.rki.coronawarnapp.submission

class TransmissionRiskVector(private val values: IntArray) {

    val raw: IntArray
        get() = values

    operator fun get(index: Int) =
        if (index < values.size) values[index] else DEFAULT_TRANSMISSION_RISK_LEVEL

    companion object {
        private const val DEFAULT_TRANSMISSION_RISK_LEVEL = 1
    }
}
