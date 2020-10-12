package de.rki.coronawarnapp.task

import dagger.MapKey
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class TaskTypeKey(val value: KClass<out Task<*, *>>)
