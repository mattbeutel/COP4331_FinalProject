package oop.project.library.scenarios;

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

class CommandScenariosTests {

    @ParameterizedTest
    @MethodSource
    public void testMul(String name, String command, Map<String, Object> expected) {
        test(command, expected);
    }

    private static Stream<Arguments> testMul() {
        return Stream.of(
            Arguments.of("Positives", """
                mul 1 2
                """, Map.of("left", 1, "right", 2)),
            Arguments.of("Negatives", """
                mul -1 -2
                """, Map.of("left", -1, "right", -2)),
            Arguments.of("Zeros", """
                mul 0 0
                """, Map.of("left", 0, "right", 0)),
            Arguments.of("Non-Numeric Left", """
                mul one 2
                """, null),
            Arguments.of("Non-Numeric Right", """
                mul 1 two
                """, null),
            Arguments.of("Non-Integer Left", """
                mul 1.0 2
                """, null),
            Arguments.of("Non-Integer Right", """
                mul 1 2.0
                """, null),
            Arguments.of("Missing Argument", """
                mul 1
                """, null),
            Arguments.of("Extraneous Argument", """
                mul 1 2 3
                """, null)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testDiv(String name, String command, Map<String, Object> expected) {
        test(command, expected);
    }

    private static Stream<Arguments> testDiv() {
        return Stream.of(
            Arguments.of("Positives", """
                div --left 1.0 --right 2.0
                """, Map.of("left", 1.0, "right", 2.0)),
            Arguments.of("Negative Integer", """
                div --left 1.0 --right -2
                """, Map.of("left", 1.0, "right", -2.0)),
            Arguments.of("Negative Decimal (FIXED BUG)", """
                div --left 1.0 --right -2.0
                """, Map.of("left", 1.0, "right", -2.0))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEcho(String name, String command, Map<String, Object> expected) {
        test(command, expected);
    }

    private static Stream<Arguments> testEcho() {
        return Stream.of(
            Arguments.of("Default", """
                echo
                """, Map.of("message", "echo,echo,echo...")),
            Arguments.of("Message", """
                echo message
                """, Map.of("message", "message"))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testSearch(String name, String command, Map<String, Object> expected) {
        test(command, expected);
    }

    private static Stream<Arguments> testSearch() {
        return Stream.of(
            Arguments.of("Lowercase", """
                search apple
                """, Map.of("term", "apple", "case-insensitive", false)),
            Arguments.of("Case Insensitive Non-Value (Bonus)", """
                search ApPlE -case-insensitive
                """, Map.of("term", "ApPlE", "case-insensitive", true)),
            Arguments.of("Case Insensitive Value", """
                search ApPlE --case-insensitive true
                """, Map.of("term", "ApPlE", "case-insensitive", true)),
            Arguments.of("Named Alias", """
                search ApPlE --i true
                """, Map.of("term", "ApPlE", "case-insensitive", true))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testDispatch(String name, String command, Map<String, Object> expected) {
        test(command, expected);
    }

    private static Stream<Arguments> testDispatch() {
        return Stream.of(
            Arguments.of("Static Valid", """
                dispatch static 1
                """, Map.of("type", "static", "value", 1)),
            Arguments.of("Static Invalid", """
                dispatch static one
                """, null),
            Arguments.of("Dynamic", """
                dispatch dynamic one
                """, Map.of("type", "dynamic", "value", "one"))
        );
    }

    private static void test(String command, Map<String, Object> expected) {
        try {
            var result = Scenarios.parse(command.stripTrailing()); //trailing newline
            Assertions.assertEquals(expected, result);
        } catch (RuntimeException e) {
            if (!e.getStackTrace()[0].getClassName().startsWith("oop.project.library.scenarios")) {
                Assertions.fail("Unexpected exception, expected an exception thrown from within a Scenario.", e);
            } else if (expected != null || e instanceof UnsupportedOperationException) {
                Assertions.fail(e.getCause() != null ? e.getCause() : e);
            }
        }
    }

}
