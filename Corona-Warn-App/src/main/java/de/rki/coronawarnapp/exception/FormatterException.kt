package de.rki.coronawarnapp.exception

class FormatterException(cause: Throwable?) :
    Exception("exception occurred during formatting", cause) {
    constructor() : this(null)
}
