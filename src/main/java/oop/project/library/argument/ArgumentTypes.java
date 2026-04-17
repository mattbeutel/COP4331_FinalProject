package oop.project.library.argument;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class ArgumentTypes {

    private ArgumentTypes() {}

    public static ArgumentType<Boolean> bool() {
        return custom("boolean", raw -> {
            if ("true".equals(raw)) {
                return true;
            }
            if ("false".equals(raw)) {
                return false;
            }
            throw new ParseFailure("Expected boolean true/false but got '" + raw + "'.");
        });
    }

    public static ArgumentType<Integer> integer() {
        return custom("integer", raw -> {
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                throw new ParseFailure("Expected integer but got '" + raw + "'.", e);
            }
        });
    }

    public static ArgumentType<Double> decimal() {
        return custom("double", raw -> {
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                throw new ParseFailure("Expected double but got '" + raw + "'.", e);
            }
        });
    }

    public static ArgumentType<String> string() {
        return custom("string", Function.identity());
    }

    public static ArgumentType<LocalDate> localDate() {
        return custom("date", raw -> {
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException e) {
                throw new ParseFailure("Expected ISO local date but got '" + raw + "'.", e);
            }
        });
    }

    public static <E extends Enum<E>> ArgumentType<E> enumType(Class<E> enumClass) {
        Objects.requireNonNull(enumClass, "enumClass");
        return custom(enumClass.getSimpleName(), raw -> {
            for (E constant : enumClass.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(raw)) {
                    return constant;
                }
            }
            throw new ParseFailure(
                    "Expected one of " + Arrays.toString(enumClass.getEnumConstants()) + " but got '" + raw + "'."
            );
        });
    }

    public static <T> ArgumentType<T> custom(String description, Function<String, T> parser) {
        return new SimpleArgumentType<>(description, raw -> {
            try {
                return parser.apply(raw);
            } catch (ParseFailure e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ParseFailure("Failed to parse " + description + " from '" + raw + "'.", e);
            }
        });
    }

    public static Validator<Integer> intRange(int minInclusive, int maxInclusive) {
        return value -> {
            if (value < minInclusive || value > maxInclusive) {
                throw new ParseFailure(
                        "Expected integer in range [" + minInclusive + ", " + maxInclusive + "] but got " + value + "."
                );
            }
        };
    }

    public static Validator<Double> doubleRange(double minInclusive, double maxInclusive) {
        return value -> {
            if (value < minInclusive || value > maxInclusive) {
                throw new ParseFailure(
                        "Expected double in range [" + minInclusive + ", " + maxInclusive + "] but got " + value + "."
                );
            }
        };
    }

    public static Validator<String> choices(String... allowedValues) {
        Set<String> allowed = new LinkedHashSet<>(Arrays.asList(allowedValues));
        return value -> {
            if (!allowed.contains(value)) {
                throw new ParseFailure("Expected one of " + allowed + " but got '" + value + "'.");
            }
        };
    }

    public static Validator<String> regex(String regex) {
        return regex(Pattern.compile(regex));
    }

    public static Validator<String> regex(Pattern pattern) {
        Objects.requireNonNull(pattern, "pattern");
        return value -> {
            if (!pattern.matcher(value).matches()) {
                throw new ParseFailure("Expected string matching regex '" + pattern.pattern() + "' but got '" + value + "'.");
            }
        };
    }

    public static <E extends Enum<E>> ArgumentType<String> enumNameLowercase(Class<E> enumClass) {
        return enumType(enumClass).map(enumClass.getSimpleName().toLowerCase(Locale.ROOT),
                value -> value.name().toLowerCase(Locale.ROOT));
    }

}
