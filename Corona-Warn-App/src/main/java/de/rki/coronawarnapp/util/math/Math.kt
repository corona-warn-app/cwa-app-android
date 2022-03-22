package de.rki.coronawarnapp.util.math

import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Round a fraction to a decimal number with the required decimal places
 * @param decimalPlaces [UInt] Number of decimal places ex: 1 -> 10 , 2 -> 10^2
 */
fun Double.roundToDecimal(decimalPlaces: UInt): Double {
    val denominator = 10.0.pow(decimalPlaces.toInt())
    return (this * denominator).roundToLong() / denominator
}
