package testhelpers

import de.rki.coronawarnapp.util.Event
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult

fun <T> beEventContent(content: T) = object : Matcher<Event<T>?> {
    override fun test(value: Event<T>?): MatcherResult {
        return MatcherResult(
            value?.getContent() == content,
            "Event content should be $content",
            "Event content should not be $content"
        )
    }
}
