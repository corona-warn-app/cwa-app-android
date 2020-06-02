package de.rki.coronawarnapp.exception

class RiskLevelCalculationException(cause: Throwable) :
    Exception("an exception occurred during risk level calculation", cause)
