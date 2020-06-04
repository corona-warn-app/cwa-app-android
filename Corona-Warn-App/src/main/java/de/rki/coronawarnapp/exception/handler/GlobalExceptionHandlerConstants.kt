package de.rki.coronawarnapp.exception.handler

object GlobalExceptionHandlerConstants {

    // name of intent extra to described that an app has crashed. Intent extra is of type boolean
    const val APP_CRASHED = "appCrashed"

    // name of intent extra that contains the stacktrace. Intent extra is of type boolean
    const val STACK_TRACE = "stackTrace"
}
