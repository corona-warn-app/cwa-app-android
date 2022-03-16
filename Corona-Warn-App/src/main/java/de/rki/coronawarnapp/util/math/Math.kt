package de.rki.coronawarnapp.util.math

import kotlin.math.roundToInt

/**
 * Round a fraction to a decimal number with the required decimal places
 * @param denominator Power of 10
 */
fun Double.roundToDecimal(denominator: Double = 100.0): Double = (this * denominator).roundToInt() / denominator
