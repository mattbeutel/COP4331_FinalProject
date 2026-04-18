package oop.project.library.command;

import oop.project.library.argument.ArgumentType;
import oop.project.library.argument.ArgumentTypes;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class ValueSpec<T> {

    enum Kind { POSITIONAL, NAMED }

    private final Kind kind;
    private final String name;
    private final ArgumentType<T> type;
    private final Set<String> aliases;
    private final boolean required;
    private final T defaultValue;
    private final boolean allowImplicitFlagValue;
    private final T implicitFlagValue;

    private ValueSpec(
            Kind kind,
            String name,
            ArgumentType<T> type,
            Set<String> aliases,
            boolean required,
            T defaultValue,
            boolean allowImplicitFlagValue,
            T implicitFlagValue
    ) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.name = requireName(name);
        this.type = Objects.requireNonNull(type, "type");
        this.aliases = Set.copyOf(aliases);
        this.required = required;
        this.defaultValue = defaultValue;
        this.allowImplicitFlagValue = allowImplicitFlagValue;
        this.implicitFlagValue = implicitFlagValue;
    }

    public static <T> ValueSpec<T> positional(String name, ArgumentType<T> type) {
        return new ValueSpec<>(Kind.POSITIONAL, name, type, Set.of(), true, null, false, null);
    }

    public static <T> ValueSpec<T> positionalWithDefault(String name, ArgumentType<T> type, T defaultValue) {
        return new ValueSpec<>(Kind.POSITIONAL, name, type, Set.of(), false, defaultValue, false, null);
    }

    public static <T> ValueSpec<T> named(String name, ArgumentType<T> type, String... aliases) {
        return new ValueSpec<>(Kind.NAMED, name, type, aliasesSet(aliases), true, null, false, null);
    }

    public static <T> ValueSpec<T> namedWithDefault(String name, ArgumentType<T> type, T defaultValue, String... aliases) {
        return new ValueSpec<>(Kind.NAMED, name, type, aliasesSet(aliases), false, defaultValue, false, null);
    }

    public static ValueSpec<Boolean> flag(String name, String... aliases) {
        return new ValueSpec<>(Kind.NAMED, name, ArgumentTypes.bool(), aliasesSet(aliases), false, false, true, true);
    }

    private static String requireName(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Argument names cannot be blank.");
        }
        return name;
    }

    private static Set<String> aliasesSet(String... aliases) {
        Set<String> result = new LinkedHashSet<>();
        for (String alias : aliases) {
            if (alias == null || alias.isBlank()) {
                throw new IllegalArgumentException("Argument aliases cannot be null or blank.");
            }
            result.add(alias);
        }
        return result;
    }

    Kind kind() {
        return kind;
    }

    public String name() {
        return name;
    }

    public ArgumentType<T> type() {
        return type;
    }

    public Set<String> aliases() {
        return aliases;
    }

    public boolean required() {
        return required;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public boolean allowImplicitFlagValue() {
        return allowImplicitFlagValue;
    }

    public T implicitFlagValue() {
        return implicitFlagValue;
    }

    boolean matchesName(String candidate) {
        return name.equals(candidate) || aliases.contains(candidate);
    }

}
