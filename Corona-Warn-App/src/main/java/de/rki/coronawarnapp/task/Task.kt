package de.rki.coronawarnapp.task

interface Task<P : TaskProgress> {

    fun run(publisher: ProgressPublisher<P>)

    fun cancel()

}
