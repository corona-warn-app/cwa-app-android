package de.rki.coronawarnapp.task

interface ProgressPublisher<P : TaskProgress> {

    fun publish(progress: P)

}
