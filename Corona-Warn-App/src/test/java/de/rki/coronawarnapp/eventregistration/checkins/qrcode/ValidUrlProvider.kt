package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class ValidUrlProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                "https://e.coronawarn.app?v=1#CAESLAgBEhFNeSBCaXJ0aGRheSBQYXJ0eRoLYXQgbXkgcGxhY2Uo04ekATD3h6QBGmoIARJgOMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq-voxQ1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UDGgQxMjM0IgQIARAC"
            ),
//            Arguments.of(
//                "https://e.coronawarn.app?v=1#"
//            ),
//            Arguments.of(
//                "https://e.coronawarn.app?v=1#"
//            ),
//            Arguments.of(
//                "https://e.coronawarn.app?v=1#"
//            )
        )
    }
}
