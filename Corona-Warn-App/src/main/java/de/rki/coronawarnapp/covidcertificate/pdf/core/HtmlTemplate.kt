package de.rki.coronawarnapp.covidcertificate.pdf.core

private val HTML_TEMPLATE = """
<!DOCTYPE html>
<html>

<head>
    <meta name="viewport" content="width=device-width, user-scalable=yes" />
    <style type="text/css">
        h6 {
            font-weight: 500;
        }
    </style>
</head>

<ul style="list-style: none;">
<!-- Add h6 and alter style above to prevent Android from inserting 
roboto font which can't be displayed on other platforms -->
<h6>
    ++certificates++
</h6>
</ul>

</html>
""".trimIndent()

fun buildHtml(builderAction: StringBuilder.() -> Unit) = HTML_TEMPLATE.replace(
    "++certificates++",
    buildString(builderAction)
)

fun StringBuilder.appendPage(page: String): StringBuilder = append("<li>$page</li>")
