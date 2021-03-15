package de.rki.coronawarnapp.eventregistration.checkins.derivetime

private const val INTERVAL_LENGTH_IN_SECONDS = 10 * 60
fun alignToInterval(timestamp:Long) = (timestamp / INTERVAL_LENGTH_IN_SECONDS) * INTERVAL_LENGTH_IN_SECONDS
//fun inRange (range, value:Int ) => {
//    if (range.minExclusive === true && value <= range.min) return false
//    else if (range.minExclusive !== true && value < range.min) return false
//    if (range.maxExclusive === true && value >= range.max) return false
//    else if (range.maxExclusive !== true && value > range.max) return false
//    return true
//}
