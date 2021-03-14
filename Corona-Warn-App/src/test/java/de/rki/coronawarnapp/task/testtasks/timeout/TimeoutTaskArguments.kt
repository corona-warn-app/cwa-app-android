package de.rki.coronawarnapp.task.testtasks.timeout

import de.rki.coronawarnapp.task.Task

data class TimeoutTaskArguments(val delay: Long = 15 * 1000L) :
    Task.Arguments
