package testhelpers.exceptions

import java.util.UUID

class TestException(private val uuid: UUID = UUID.randomUUID()) : Exception()
