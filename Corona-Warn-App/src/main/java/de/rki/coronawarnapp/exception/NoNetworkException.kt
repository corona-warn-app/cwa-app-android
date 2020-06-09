package de.rki.coronawarnapp.exception

class NoNetworkException(cause: Throwable) :
    Exception("The application is not connected to the internet", cause)
