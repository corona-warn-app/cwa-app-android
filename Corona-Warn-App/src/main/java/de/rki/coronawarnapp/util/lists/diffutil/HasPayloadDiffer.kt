package de.rki.coronawarnapp.util.lists.diffutil

interface HasPayloadDiffer {
    fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
}
