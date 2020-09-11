package de.rki.coronawarnapp.util.viewmodel

import dagger.MapKey
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class VDCKey(val value: KClass<out VDC>)
