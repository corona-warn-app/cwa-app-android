package de.rki.coronawarnapp.util.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

inline fun <reified T> ObjectMapper.writeValue(data: T, file: File) = file.bufferedWriter().use { writer ->
    writeValue(writer, data)
    writer.flush()
}
