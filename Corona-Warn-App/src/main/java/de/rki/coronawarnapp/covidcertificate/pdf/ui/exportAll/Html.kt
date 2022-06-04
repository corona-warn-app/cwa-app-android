package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

val HTML_TEMPLATE = """
<!DOCTYPE html>
<html>

<head>
    <meta name="viewport" content="width=device-width, user-scalable=yes" />
    <style>
        ul.separator {
            list-style: none;
            padding: 0;
            width: device-width;
        }

        ul.separator li {
            padding: .5em 0;
            border-bottom: 1px solid #CCC;
        }
    </style>
</head>

<ul class="separator">
    ++certificates++
</ul>

</html>
""".trimIndent()
