package de.rki.coronawarnapp.util.math

import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Round a fraction to a decimal number with the required decimal places
 * @param decimalPlacesNumber [UInt]
 */
fun Double.roundToDecimal(decimalPlacesNumber: UInt): Double {
    val denominator = 10.0.pow(decimalPlacesNumber.toInt())
    return (this * denominator).roundToLong() / denominator
}
