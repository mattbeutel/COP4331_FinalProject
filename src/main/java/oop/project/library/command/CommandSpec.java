package oop.project.library.command;

import oop.project.library.argument.ArgumentType;
import oop.project.library.argument.ArgumentTypes;
import oop.project.library.argument.ParseException;
import oop.project.library.input.BasicArgs;
import oop.project.library.input.Input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable definition of one command's argument structure.
 */
public final class CommandSpec {

    private record PositionalArgument<T>(String name, ArgumentType<T> type, boolean required, T defaultValue) {}

    private record NamedArgument<T>(
            String name,
            ArgumentType<T> type,
            Set<String> aliases,
            boolean required,
            T defaultValue,
            boolean acceptsImplicitValue,
            T implicitValue
    ) {}

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private final List<PositionalArgument<?>> positionalArguments = new ArrayList<>();
        private final List<NamedArgument<?>> namedArguments = new ArrayList<>();
        private final Map<String, CommandSpec> subcommands = new LinkedHashMap<>();
        private final Set<String> canonicalArgumentNames = new LinkedHashSet<>();
        private final Set<String> namedKeys = new LinkedHashSet<>();
        private boolean hasOptionalPositional = false;

        private Builder(String name) {
            this.name = requireName(name, "Command name");
        }

        /**
         * Adds a required positional argument parsed by the supplied argument type.
         */
        public <T> Builder addPositional(String name, ArgumentType<T> type) {
            ensureNoSubcommands("positional arguments");
            if (hasOptionalPositional) {
                throw new IllegalArgumentException("Required positional arguments cannot be added after optional positional arguments.");
            }
            String canonicalName = registerArgumentName(name, "Positional argument name");
            positionalArguments.add(new PositionalArgument<>(canonicalName, Objects.requireNonNull(type, "type"), true, null));
            return this;
        }

        public <T> Builder addOptionalPositional(String name, ArgumentType<T> type, T defaultValue) {
            ensureNoSubcommands("positional arguments");
            String canonicalName = registerArgumentName(name, "Positional argument name");
            positionalArguments.add(new PositionalArgument<>(canonicalName, Objects.requireNonNull(type, "type"), false, defaultValue));
            hasOptionalPositional = true;
            return this;
        }

        /**
         * Adds a required named argument and any aliases that should map to the same value.
         */
        public <T> Builder addNamed(String name, ArgumentType<T> type, String... aliases) {
            ensureNoSubcommands("named arguments");
            namedArguments.add(new NamedArgument<>(
                    registerNamedName(name),
                    Objects.requireNonNull(type, "type"),
                    registerAliases(aliases),
                    true,
                    null,
                    false,
                    null
            ));
            return this;
        }

        public <T> Builder addOptionalNamed(String name, ArgumentType<T> type, T defaultValue, String... aliases) {
            ensureNoSubcommands("named arguments");
            namedArguments.add(new NamedArgument<>(
                    registerNamedName(name),
                    Objects.requireNonNull(type, "type"),
                    registerAliases(aliases),
                    false,
                    defaultValue,
                    false,
                    null
            ));
            return this;
        }

        public Builder addFlag(String name, String... aliases) {
            ensureNoSubcommands("named arguments");
            namedArguments.add(new NamedArgument<>(
                    registerNamedName(name),
                    ArgumentTypes.bool(),
                    registerAliases(aliases),
                    false,
                    false,
                    true,
                    true
            ));
            return this;
        }

        public Builder addSubcommand(String token, CommandSpec spec) {
            ensureNoArguments("subcommands");
            String normalizedToken = requireName(token, "Subcommand token");
            Objects.requireNonNull(spec, "spec");
            if (subcommands.putIfAbsent(normalizedToken, spec) != null) {
                throw new IllegalArgumentException("Duplicate subcommand token '" + normalizedToken + "'.");
            }
            return this;
        }

        public CommandSpec build() {
            return new CommandSpec(name, positionalArguments, namedArguments, subcommands);
        }

        private String registerArgumentName(String rawName, String label) {
            String name = requireName(rawName, label);
            if (!canonicalArgumentNames.add(name)) {
                throw new IllegalArgumentException("Duplicate argument name '" + name + "'.");
            }
            return name;
        }

        private String registerNamedName(String rawName) {
            String name = registerArgumentName(rawName, "Named argument name");
            if (!namedKeys.add(name)) {
                throw new IllegalArgumentException("Duplicate named argument key '" + name + "'.");
            }
            return name;
        }

        private Set<String> registerAliases(String... aliases) {
            Set<String> normalizedAliases = new LinkedHashSet<>();
            for (String alias : aliases) {
                String normalized = requireName(alias, "Named argument alias");
                if (!normalizedAliases.add(normalized)) {
                    throw new IllegalArgumentException("Duplicate alias '" + normalized + "'.");
                }
                if (!namedKeys.add(normalized)) {
                    throw new IllegalArgumentException("Duplicate named argument key '" + normalized + "'.");
                }
            }
            return Set.copyOf(normalizedAliases);
        }

        private void ensureNoSubcommands(String thingBeingAdded) {
            if (!subcommands.isEmpty()) {
                throw new IllegalArgumentException("Cannot add " + thingBeingAdded + " to a command that already has subcommands.");
            }
        }

        private void ensureNoArguments(String thingBeingAdded) {
            if (!positionalArguments.isEmpty() || !namedArguments.isEmpty()) {
                throw new IllegalArgumentException("Cannot add " + thingBeingAdded + " to a command that already has direct arguments.");
            }
        }
    }

    private final String name;
    private final List<PositionalArgument<?>> positionalArguments;
    private final List<NamedArgument<?>> namedArguments;
    private final Map<String, CommandSpec> subcommands;
    private final Map<String, NamedArgument<?>> namedLookup;

    private CommandSpec(
            String name,
            List<PositionalArgument<?>> positionalArguments,
            List<NamedArgument<?>> namedArguments,
            Map<String, CommandSpec> subcommands
    ) {
        this.name = name;
        this.positionalArguments = List.copyOf(positionalArguments);
        this.namedArguments = List.copyOf(namedArguments);
        this.subcommands = Map.copyOf(subcommands);
        this.namedLookup = buildNamedLookup(namedArguments);
    }

    public ParsedCommand parse(String rawArguments) {
        return parse(new Input(rawArguments).parseBasicArgs());
    }

    public ParsedCommand parse(BasicArgs args) {
        Objects.requireNonNull(args, "args");
        if (!subcommands.isEmpty()) {
            return parseSubcommand(args);
        }

        Map<String, Object> values = new LinkedHashMap<>();
        parsePositionalArguments(args.positional(), values);
        parseNamedArguments(args.named(), values);
        return new ParsedCommand(values);
    }

    private ParsedCommand parseSubcommand(BasicArgs args) {
        if (args.positional().isEmpty()) {
            throw new ParseException("Command '" + name + "' requires a subcommand.");
        }

        String token = args.positional().get(0);
        CommandSpec subcommand = subcommands.get(token);
        if (subcommand == null) {
            throw new ParseException("Unknown subcommand '" + token + "' for command '" + name + "'.");
        }

        BasicArgs remainingArgs = new BasicArgs(
                new ArrayList<>(args.positional().subList(1, args.positional().size())),
                new LinkedHashMap<>(args.named())
        );
        ParsedCommand parsedSubcommand = subcommand.parse(remainingArgs);
        return new ParsedCommand(parsedSubcommand.values(), token);
    }

    private void parsePositionalArguments(List<String> rawPositionals, Map<String, Object> values) {
        if (rawPositionals.size() > positionalArguments.size()) {
            throw new ParseException("Command '" + name + "' received too many positional arguments.");
        }

        for (int index = 0; index < positionalArguments.size(); index++) {
            PositionalArgument<?> argument = positionalArguments.get(index);
            if (index < rawPositionals.size()) {
                values.put(argument.name(), parseRaw(argument.name(), argument.type(), rawPositionals.get(index)));
            } else if (argument.required()) {
                throw new ParseException("Missing positional argument '" + argument.name() + "'.");
            } else {
                values.put(argument.name(), argument.defaultValue());
            }
        }
    }

    private void parseNamedArguments(Map<String, String> rawNamedArguments, Map<String, Object> values) {
        Set<String> seenCanonicalNames = new LinkedHashSet<>();
        for (Map.Entry<String, String> entry : rawNamedArguments.entrySet()) {
            NamedArgument<?> argument = namedLookup.get(entry.getKey());
            if (argument == null) {
                throw new ParseException("Unknown named argument '" + entry.getKey() + "'.");
            }
            if (!seenCanonicalNames.add(argument.name())) {
                throw new ParseException("Named argument '" + argument.name() + "' was provided multiple times.");
            }

            String rawValue = entry.getValue();
            if (rawValue.isEmpty()) {
                if (!argument.acceptsImplicitValue()) {
                    throw new ParseException("Named argument '" + argument.name() + "' requires a value.");
                }
                values.put(argument.name(), argument.implicitValue());
            } else {
                values.put(argument.name(), parseRaw(argument.name(), argument.type(), rawValue));
            }
        }

        for (NamedArgument<?> argument : namedArguments) {
            if (!values.containsKey(argument.name())) {
                if (argument.required()) {
                    throw new ParseException("Missing named argument '" + argument.name() + "'.");
                }
                values.put(argument.name(), argument.defaultValue());
            }
        }
    }

    private static Map<String, NamedArgument<?>> buildNamedLookup(List<NamedArgument<?>> namedArguments) {
        Map<String, NamedArgument<?>> lookup = new LinkedHashMap<>();
        for (NamedArgument<?> argument : namedArguments) {
            if (lookup.putIfAbsent(argument.name(), argument) != null) {
                throw new IllegalArgumentException("Duplicate named argument key '" + argument.name() + "'.");
            }
            for (String alias : argument.aliases()) {
                if (lookup.putIfAbsent(alias, argument) != null) {
                    throw new IllegalArgumentException("Duplicate named argument key '" + alias + "'.");
                }
            }
        }
        return Map.copyOf(lookup);
    }

    private static Object parseRaw(String argumentName, ArgumentType<?> type, String rawValue) {
        try {
            return type.parse(rawValue);
        } catch (ParseException e) {
            throw new ParseException("Invalid value for '" + argumentName + "': " + e.getMessage(), e);
        }
    }

    private static String requireName(String name, String label) {
        Objects.requireNonNull(name, label);
        if (name.isBlank()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return name;
    }

}
