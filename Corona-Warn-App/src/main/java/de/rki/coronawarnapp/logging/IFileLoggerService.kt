package de.rki.coronawarnapp.logging

import java.io.File

@Deprecated("better idea now")
interface IFileLoggerService {

    fun log(logElement: LogElement)
    fun getFile(): File
    fun reset()
}
