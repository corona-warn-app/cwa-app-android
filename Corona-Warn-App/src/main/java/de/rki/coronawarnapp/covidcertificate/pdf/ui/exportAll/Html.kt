package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

val HTML_TEMPLATE = """
<!DOCTYPE html>
<html>
<style>
    .dcc_container {
        isolation: isolate
    }
</style>
<ol>
    ++certificates++
</ol>

</html>
""".trimIndent()
