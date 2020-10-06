package de.rki.coronawarnapp.logging

import java.io.File

interface IFileLoggerService {

    fun log(logElement: LogElement)
    fun getFile(): File
    fun reset()
}
