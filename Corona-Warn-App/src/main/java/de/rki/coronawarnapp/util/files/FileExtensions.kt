package de.rki.coronawarnapp.util.files

import java.io.File

fun File.determineMimeType(): String = when {
    name.endsWith(".zip") -> "application/zip"
    name.endsWith(".pdf") -> "application/pdf"
    else -> throw UnsupportedOperationException("Unsupported MIME type: $path")
}
