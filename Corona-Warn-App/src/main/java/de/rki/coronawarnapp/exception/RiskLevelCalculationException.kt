package de.rki.coronawarnapp.exception

class RiskLevelCalculationException(cause: Throwable) :
    Exception("an exception occured during risk level calculation", cause)
