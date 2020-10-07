package de.rki.coronawarnapp.task

import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.DateTimeZone
import java.util.UUID
import javax.inject.Inject

class TaskStateFactory @Inject constructor(
    private val timeStamper: TimeStamper
) {

    fun <P : TaskProgress> createState(task: Task<P>, type: TaskType) =
        TaskState(UUID.randomUUID(), type, timeStamper.nowUTC.toDateTime(DateTimeZone.UTC))

}
