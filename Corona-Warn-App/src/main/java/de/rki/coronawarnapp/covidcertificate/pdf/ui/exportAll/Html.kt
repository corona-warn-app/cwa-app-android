package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

val HTML_TEMPLATE = """
<!DOCTYPE html>
<html>
<style>
    ul.separator {
        list-style: none;
        padding: 0;
        width: 100%;
    }

    ul.separator li {
        padding: .5em 0;
        border-bottom: 3px solid #CCC;
    }
</style>

<ul class="separator">
    ++certificates++
</ul>

</html>
""".trimIndent()
