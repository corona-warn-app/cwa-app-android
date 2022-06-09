package de.rki.coronawarnapp.covidcertificate.pdf.core

private val HTML_TEMPLATE = """
<!DOCTYPE html>
<html>

<head>
    <meta name="viewport" content="width=device-width, user-scalable=yes" />
</head>

<ul style="list-style: none;">
    ++certificates++
</ul>

</html>
""".trimIndent()

fun buildHtml(builderAction: StringBuilder.() -> Unit) = HTML_TEMPLATE.replace(
    "++certificates++",
    buildString(builderAction)
)

fun StringBuilder.appendPage(page: String): StringBuilder = append("<li>$page</li>")
