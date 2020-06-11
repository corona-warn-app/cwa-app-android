package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

class NotEnoughSpaceOnDiskException(cause: Throwable? = null) : ReportedException(
    ErrorCodes.NOT_ENOUGH_AVAILABLE_SPACE_ON_DISK.code,
    "the app detected that not enough storage space is available for the required operation",
    cause,
    R.string.errors_not_enough_device_storage
)
