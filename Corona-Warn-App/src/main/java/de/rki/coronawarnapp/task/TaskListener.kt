package de.rki.coronawarnapp.task

interface TaskListener {

    val isAlive: Boolean

    fun onStateChanged(state: TaskState)

    fun onError(state: TaskState)

    fun onProgress(state: TaskState, progress: TaskProgress)

}
