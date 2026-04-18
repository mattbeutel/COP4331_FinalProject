package oop.project.library.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ParsedCommand {

    private final Map<String, Object> values;
    private final String subcommandName;

    ParsedCommand(Map<String, Object> values) {
        this(values, null);
    }

    ParsedCommand(Map<String, Object> values, String subcommandName) {
        this.values = new LinkedHashMap<>(values);
        this.subcommandName = subcommandName;
    }

    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(values);
    }

    public Optional<String> subcommandName() {
        return Optional.ofNullable(subcommandName);
    }

    public String requireSubcommandName() {
        if (subcommandName == null) {
            throw new IllegalStateException("No subcommand was selected.");
        }
        return subcommandName;
    }

    public boolean contains(String name) {
        return values.containsKey(name);
    }

    public Object get(String name) {
        if (!values.containsKey(name)) {
            throw new IllegalArgumentException("Unknown argument '" + name + "'.");
        }
        return values.get(name);
    }

    public <T> T get(String name, Class<T> type) {
        Objects.requireNonNull(type, "type");
        Object value = get(name);
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
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
