package oop.project.library.command;

import oop.project.library.argument.ParseException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable result of parsing one command invocation.
 */
public record ParsedCommand(Map<String, Object> values, String subcommandName) {

    public ParsedCommand {
        Objects.requireNonNull(values, "values");
        values = Map.copyOf(new LinkedHashMap<>(values));
    }

    public ParsedCommand(Map<String, Object> values) {
        this(values, null);
    }

    public Optional<String> subcommandNameOption() {
        return Optional.ofNullable(subcommandName);
    }

    public boolean contains(String name) {
        return values.containsKey(name);
    }

    public Object get(String name) {
        if (!values.containsKey(name)) {
            throw new ParseException("Unknown argument '" + name + "'.");
        }
        return values.get(name);
    }

    /**
     * Returns a parsed value while also verifying the caller's expected static type.
     *
     * @param name argument name to extract
     * @param type expected runtime type
     * @return parsed value cast to the requested type
     * @throws ParseException when the argument is missing or has a different type
     */
    public <T> T get(String name, Class<T> type) {
        Objects.requireNonNull(type, "type");
        Object value = get(name);
        if (!type.isInstance(value)) {
            throw new ParseException(
                    "Argument '" + name + "' is a " + value.getClass().getSimpleName() + ", not a " + type.getSimpleName() + "."
            );
        }
        return type.cast(value);
    }

    public int getInt(String name) {
        return get(name, Integer.class);
    }

    public double getDouble(String name) {
        return get(name, Double.class);
    }

    public boolean getBoolean(String name) {
        return get(name, Boolean.class);
    }

    public String getString(String name) {
        return get(name, String.class);
    }

}
