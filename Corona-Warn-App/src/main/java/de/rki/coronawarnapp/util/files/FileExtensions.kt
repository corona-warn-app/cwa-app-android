package de.rki.coronawarnapp.util.files

import java.io.File

fun File.determineMimeType(): String = when {
    name.endsWith(".zip") -> "application/zip"
    else -> throw UnsupportedOperationException("Unsupported MIME type: $path")
}
