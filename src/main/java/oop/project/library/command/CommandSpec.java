package oop.project.library.command;

import oop.project.library.argument.ParseFailure;
import oop.project.library.input.BasicArgs;
import oop.project.library.input.Input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CommandSpec {

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private final List<ValueSpec<?>> positionalSpecs = new ArrayList<>();
        private final List<ValueSpec<?>> namedSpecs = new ArrayList<>();
        private final Map<String, CommandSpec> subcommands = new LinkedHashMap<>();
        private final Set<String> positionalNames = new LinkedHashSet<>();
        private final Set<String> namedKeys = new LinkedHashSet<>();

        private Builder(String name) {
            this.name = requireName(name, "Command name");
        }

        public Builder positional(ValueSpec<?> spec) {
            requireKind(spec, ValueSpec.Kind.POSITIONAL);
            if (!positionalNames.add(spec.name())) {
                throw new IllegalArgumentException("Duplicate positional argument name '" + spec.name() + "'.");
            }
            positionalSpecs.add(spec);
            return this;
        }

        public Builder named(ValueSpec<?> spec) {
            requireKind(spec, ValueSpec.Kind.NAMED);
            registerNamedKey(spec.name());
            for (String alias : spec.aliases()) {
                registerNamedKey(alias);
            }
            namedSpecs.add(spec);
            return this;
        }

        public Builder subcommand(String token, CommandSpec spec) {
            String normalizedToken = requireName(token, "Subcommand token");
            Objects.requireNonNull(spec, "spec");
            if (subcommands.putIfAbsent(normalizedToken, spec) != null) {
                throw new IllegalArgumentException("Duplicate subcommand token '" + normalizedToken + "'.");
            }
            return this;
        }

        public CommandSpec build() {
            return new CommandSpec(name, positionalSpecs, namedSpecs, subcommands);
        }

        private void registerNamedKey(String key) {
            if (!namedKeys.add(key)) {
                throw new IllegalArgumentException("Duplicate named argument key '" + key + "'.");
            }
        }

        private static void requireKind(ValueSpec<?> spec, ValueSpec.Kind expected) {
            Objects.requireNonNull(spec, "spec");
            if (spec.kind() != expected) {
                throw new IllegalArgumentException("Expected a " + expected + " specification.");
            }
        }
    }

    private final String name;
    private final List<ValueSpec<?>> positionalSpecs;
    private final List<ValueSpec<?>> namedSpecs;
    private final Map<String, CommandSpec> subcommands;

    private CommandSpec(
            String name,
            List<ValueSpec<?>> positionalSpecs,
            List<ValueSpec<?>> namedSpecs,
            Map<String, CommandSpec> subcommands
    ) {
        this.name = name;
        this.positionalSpecs = List.copyOf(positionalSpecs);
        this.namedSpecs = List.copyOf(namedSpecs);
        this.subcommands = Map.copyOf(subcommands);
    }

    public ParsedCommand parse(String rawArguments) {
        return parse(tokenize(rawArguments));
    }

    public ParsedCommand parse(BasicArgs args) {
        if (!subcommands.isEmpty()) {
            return parseWithSubcommands(args);
        }

        Map<String, Object> values = new LinkedHashMap<>();
        parsePositionals(args, values);
        parseNamed(args, values);
        return new ParsedCommand(values);
    }

    private static BasicArgs tokenize(String rawArguments) {
        Input input = new Input(rawArguments);
        List<Input.Value> tokens = new ArrayList<>();
        while (true) {
            Input.Value value = input.parseValue().orElse(null);
            if (value == null) {
                break;
            }
            tokens.add(value);
        }

        BasicArgs args = new BasicArgs(new ArrayList<>(), new LinkedHashMap<>());
        for (int i = 0; i < tokens.size(); i++) {
            Input.Value token = tokens.get(i);
            switch (token) {
                case Input.Value.Literal(String literal) -> args.positional().add(literal);
                case Input.Value.QuotedString(String quoted) -> args.positional().add(quoted);
                case Input.Value.SingleFlag(String flagName) -> i += attachNamedValue(args, tokens, i, flagName);
                case Input.Value.DoubleFlag(String flagName) -> i += attachNamedValue(args, tokens, i, flagName);
            }
        }
        return args;
    }

    private static int attachNamedValue(BasicArgs args, List<Input.Value> tokens, int index, String name) {
        if (index + 1 >= tokens.size()) {
            args.named().put(name, "");
            return 0;
        }

        Input.Value next = tokens.get(index + 1);
        switch (next) {
            case Input.Value.Literal(String literal) -> {
                args.named().put(name, literal);
                return 1;
            }
            case Input.Value.QuotedString(String quoted) -> {
                args.named().put(name, quoted);
                return 1;
            }
            case Input.Value.SingleFlag(String ignoredSingle) -> {
                args.named().put(name, "");
                return 0;
            }
            case Input.Value.DoubleFlag(String ignoredDouble) -> {
                args.named().put(name, "");
                return 0;
            }
        }
    }

    private ParsedCommand parseWithSubcommands(BasicArgs args) {
        if (args.positional().isEmpty()) {
            throw new ParseFailure("Command '" + name + "' requires a subcommand.");
        }

        String token = args.positional().getFirst();
        CommandSpec subcommand = subcommands.get(token);
        if (subcommand == null) {
            throw new ParseFailure("Unknown subcommand '" + token + "' for command '" + name + "'.");
        }

        BasicArgs remaining = new BasicArgs(
                new ArrayList<>(args.positional().subList(1, args.positional().size())),
                new LinkedHashMap<>(args.named())
        );
        ParsedCommand parsedSubcommand = subcommand.parse(remaining);
        return new ParsedCommand(parsedSubcommand.asMap(), token);
    }

    private void parsePositionals(BasicArgs args, Map<String, Object> values) {
        if (args.positional().size() > positionalSpecs.size()) {
            throw new ParseFailure("Command '" + name + "' received too many positional arguments.");
        }

        for (int i = 0; i < positionalSpecs.size(); i++) {
            ValueSpec<?> spec = positionalSpecs.get(i);
            if (i < args.positional().size()) {
                values.put(spec.name(), parseRaw(spec, args.positional().get(i)));
            } else if (spec.required()) {
                throw new ParseFailure("Missing positional argument '" + spec.name() + "'.");
            } else {
                values.put(spec.name(), spec.defaultValue());
            }
        }
    }

    private void parseNamed(BasicArgs args, Map<String, Object> values) {
        for (Map.Entry<String, String> entry : args.named().entrySet()) {
            ValueSpec<?> spec = resolveNamed(entry.getKey());
            if (spec == null) {
                throw new ParseFailure("Unknown named argument '" + entry.getKey() + "'.");
            }

            String raw = entry.getValue();
            if (raw.isEmpty()) {
                if (!spec.allowImplicitFlagValue()) {
                    throw new ParseFailure("Named argument '" + spec.name() + "' requires a value.");
                }
                values.put(spec.name(), spec.implicitFlagValue());
            } else {
                values.put(spec.name(), parseRaw(spec, raw));
            }
        }

        for (ValueSpec<?> spec : namedSpecs) {
            if (!values.containsKey(spec.name())) {
                if (spec.required()) {
                    throw new ParseFailure("Missing named argument '" + spec.name() + "'.");
                }
                values.put(spec.name(), spec.defaultValue());
            }
        }
    }

    private ValueSpec<?> resolveNamed(String key) {
        for (ValueSpec<?> spec : namedSpecs) {
            if (spec.matchesName(key)) {
                return spec;
            }
        }
        return null;
    }

    private static Object parseRaw(ValueSpec<?> spec, String raw) {
        try {
            return spec.type().parse(raw);
        } catch (ParseFailure e) {
            throw new ParseFailure("Invalid value for '" + spec.name() + "': " + e.getMessage(), e);
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
