package de.rki.coronawarnapp.presencetracing.warning

data class WarningPackageIds (
    val oldest: WarningPackageId,
    val latest: WarningPackageId
)

typealias WarningPackageId = Long
