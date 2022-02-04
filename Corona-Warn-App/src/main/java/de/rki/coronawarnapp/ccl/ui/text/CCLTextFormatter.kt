package de.rki.coronawarnapp.ccl.ui.text

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import dagger.Reusable
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CCLJsonFunctions
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.getDefaultInputParameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Parameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PluralText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTimeDependentText
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.serialization.BaseJackson
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@Reusable
class CCLTextFormatter @Inject constructor(
    private val cclJsonFunctions: CCLJsonFunctions,
    @BaseJackson private val mapper: ObjectMapper
) {
    suspend fun format(
        cclText: CCLText?,
        locale: Locale = Locale.getDefault()
    ): String = runCatching {
        when (cclText) {
            is PluralText -> cclText.formatPlural(locale)
            is SingleText -> cclText.formatSingle(locale)
            is SystemTimeDependentText -> format(cclText.formatSystemTimeDependent(locale))
            else -> null
        }
    }.getOrElse {
        Timber.w(it, "CCLText.format() failed")
        null
    }.orEmpty()

    fun formatFaqAnchor(
        faqAnchor: String?,
        locale: Locale = Locale.getDefault()
    ) = when {
        faqAnchor.isNullOrBlank() -> null
        else -> {
            val lang = if (locale.language == Locale.GERMAN.language) locale.language else Locale.ENGLISH.language
            "https://www.coronawarn.app/$lang/faq/#$faqAnchor"
        }
    }

    private fun SingleText.formatSingle(
        locale: Locale
    ): String? {
        val text = localizedText[locale.language]
            ?: localizedText[EN] // Default for other languages
            ?: localizedText[DE] // Default for EN

        return text
            ?.replace("%@", "%s")
            ?.format(*parameters.convertValues(locale))
    }

    private fun PluralText.formatPlural(
        locale: Locale
    ): String? {
        val quantity = quantity()
        val quantityText = localizedText[locale.language]
            ?: localizedText[EN] // Default for other languages
            ?: localizedText[DE] // Default for EN
            ?: return null

        val text = pluralText(quantity, quantityText, locale)
        return text
            .replace("%@", "%s")
            .format(*parameters.convertValues(locale))
    }

    private fun PluralText.quantity(): Int = quantity ?: quantityFromIndex()

    private fun PluralText.quantityFromIndex(): Int {
        val param = parameters[quantityParameterIndex ?: 0]
        return when (param.type) {
            Parameters.Type.NUMBER -> param.toNumber()
            else -> error("`quantity` can't be derived from param=$param")
        }
    }

    private suspend fun SystemTimeDependentText.formatSystemTimeDependent(locale: Locale): CCLText? {

        val functionName = functionName

        val defaultParameters = getDefaultInputParameters(DateTime.now()).toObjectNode()
        val parameters = parameters

        // TODO: merge defaultParameters and parameters
        val output = cclJsonFunctions.evaluateFunction(functionName, parameters)
        mapper.treeToValue(output, CCLText::class.java)

        return TODO()
    }

    private fun List<Parameters>.convertValues(locale: Locale): Array<Any> =
        map { parameter -> parameter.covertValue(locale) }.toTypedArray()

    private fun Parameters.covertValue(locale: Locale) = when (type) {
        Parameters.Type.STRING -> value.toString()
        Parameters.Type.NUMBER -> toNumber()
        Parameters.Type.BOOLEAN -> toBoolean()
        Parameters.Type.LOCAL_DATE -> toLocalDate(locale)
        Parameters.Type.LOCAL_DATE_TIME -> toLocalDateTime(locale)
        Parameters.Type.UTC_DATE -> toUTCDate(locale)
        Parameters.Type.UTC_DATE_TIME -> toUTCDateTime(locale)
    }

    private fun Parameters.toUTCDateTime(locale: Locale): String {
        return runCatching {
            Instant.parse(value.toString()).run {
                val date = toString(DateTimeFormat.shortDate().withLocale(locale))
                val time = toString(DateTimeFormat.shortTime().withLocale(locale))
                "$date, $time"
            }
        }.getOrElse {
            Timber.e(it, "Parameters.toUTCDateTime() failed")
            ""
        }
    }

    private fun Parameters.toUTCDate(locale: Locale): String {
        return runCatching {
            Instant.parse(value.toString())
                .toString(DateTimeFormat.shortDate().withLocale(locale))
        }.getOrElse {
            Timber.e(it, "Parameters.toUTCDate() failed")
            ""
        }
    }

    private fun Parameters.toLocalDateTime(locale: Locale): String {
        return runCatching {
            Instant.parse(value.toString()).toLocalDateTimeUserTz().run {
                val date = toString(DateTimeFormat.shortDate().withLocale(locale))
                val time = toString(DateTimeFormat.shortTime().withLocale(locale))
                "$date, $time"
            }
        }.getOrElse {
            Timber.e(it, "Parameters.toLocalDateTime() failed")
            ""
        }
    }

    private fun Parameters.toLocalDate(locale: Locale): String {
        return runCatching {
            Instant.parse(value.toString()).toLocalDateTimeUserTz()
                .toString(DateTimeFormat.shortDate().withLocale(locale))
        }.getOrElse {
            Timber.e(it, "Parameters.toLocalDate() failed")
            ""
        }
    }

    private fun Parameters.toNumber(): Int = runCatching {
        (value as Number).toInt()
    }.getOrElse {
        Timber.e(it, "Parameters.toNumber() failed")
        0
    }

    private fun Parameters.toBoolean(): Boolean = runCatching {
        value as Boolean
    }.getOrElse {
        Timber.e(it, " Parameters.toBoolean() failed")
        false
    }

    private fun Any.toObjectNode(): ObjectNode = mapper.valueToTree(this)

    companion object {
        private const val EN = "en"
        private const val DE = "de"
    }
}
