package oop.project.library.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public final class Input {

    public sealed interface Value {
        record Literal(String value) implements Value {}
        record QuotedString(String value) implements Value {}
        record SingleFlag(String name) implements Value {}
        record DoubleFlag(String name) implements Value {}
    }

    private final char[] chars;
    private int index = 0;

    public Input(String input) {
        chars = input.toCharArray();
    }

    public BasicArgs parseBasicArgs() {
        var args = new BasicArgs(new ArrayList<>(), new HashMap<>());
        while (true) {
            switch (parseValue().orElse(null)) {
                case null -> { return args; }
                case Value.Literal(String value) -> args.positional().add(value);
                case Value.QuotedString(String value) -> args.positional().add(value);
                case Value.SingleFlag(String name) -> args.named().put(name, "");
                case Value.DoubleFlag(String name) -> {
                    switch (parseValue().orElse(null)) {
                        case Value.Literal(String value) -> args.named().put(name, value);
                        case Value.QuotedString(String value) -> args.named().put(name, value);
                        case null, default -> throw new RuntimeException("Double flag --" + name + " is missing a value @ index " + index + ".");
                    }
                }
            }
        }
    }

    public Optional<Value> parseValue() {
        while (index < chars.length && chars[index] == ' ') { index++; }
        if (index >= chars.length) {
            return Optional.empty();
        } else if (chars[index] == '"') {
            var start = index;
            do { index++; } while (index < chars.length && chars[index] != '"');
            if (index >= chars.length) {
                throw new RuntimeException("Unterminated quoted string @ index " + start + ".");
            }
            var value = new String(chars, start + 1, index - start - 1);
            index++;
            return Optional.of(new Value.QuotedString(value));
        } else {
            var start = index;
            do { index++; } while (index < chars.length && chars[index] != ' ' && chars[index] != '"');
            if (index < chars.length && chars[index] == '"') {
                throw new RuntimeException("Invalid quote within literal @ index " + index + ".");
            }
            var value = new String(chars, start, index - start);
            if (value.startsWith("-") && value.length() > 1 && Character.isLetter(value.charAt(1))) {
                return Optional.of(new Value.SingleFlag(value.substring(1)));
            } else if (value.startsWith("--") && value.length() > 2 && Character.isLetter(value.charAt(2))) {
                return Optional.of(new Value.DoubleFlag(value.substring(2)));
            } else {
                return Optional.of(new Value.Literal(value));
            }
        }
    }

}
