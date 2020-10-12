package de.rki.coronawarnapp.task

import kotlin.reflect.KClass

class MissingTaskFactory(
    requestClass: KClass<out TaskRequest>
) : IllegalStateException("No task factory mapped for $requestClass")
