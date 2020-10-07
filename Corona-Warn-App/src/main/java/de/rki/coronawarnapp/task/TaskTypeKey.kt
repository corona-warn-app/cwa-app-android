package de.rki.coronawarnapp.task

import dagger.MapKey

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class TaskTypeKey(val value: TaskType)
