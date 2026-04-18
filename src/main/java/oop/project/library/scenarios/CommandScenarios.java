package oop.project.library.scenarios;

import oop.project.library.argument.ArgumentTypes;
import oop.project.library.command.CommandSpec;
import oop.project.library.command.ParsedCommand;
import oop.project.library.command.ValueSpec;

import java.util.Map;

public final class CommandScenarios {

    private static final CommandSpec MUL = CommandSpec.builder("mul")
            .positional(ValueSpec.positional("left", ArgumentTypes.integer()))
            .positional(ValueSpec.positional("right", ArgumentTypes.integer()))
            .build();

    private static final CommandSpec DIV = CommandSpec.builder("div")
            .named(ValueSpec.named("left", ArgumentTypes.decimal()))
            .named(ValueSpec.named("right", ArgumentTypes.decimal()))
            .build();

    private static final CommandSpec ECHO = CommandSpec.builder("echo")
            .positional(ValueSpec.positionalWithDefault("message", ArgumentTypes.string(), "echo,echo,echo..."))
            .build();

    private static final CommandSpec SEARCH = CommandSpec.builder("search")
            .positional(ValueSpec.positional("term", ArgumentTypes.string()))
            .named(ValueSpec.flag("case-insensitive", "i"))
            .build();

    private static final CommandSpec STATIC_DISPATCH = CommandSpec.builder("static")
            .positional(ValueSpec.positional("value", ArgumentTypes.integer()))
            .build();

    private static final CommandSpec DYNAMIC_DISPATCH = CommandSpec.builder("dynamic")
            .positional(ValueSpec.positional("value", ArgumentTypes.string()))
            .build();

    private static final CommandSpec DISPATCH = CommandSpec.builder("dispatch")
            .subcommand("static", STATIC_DISPATCH)
            .subcommand("dynamic", DYNAMIC_DISPATCH)
            .build();

    public static Map<String, Object> mul(String arguments) throws RuntimeException {
        try {
            ParsedCommand parsed = MUL.parse(arguments);
            int left = parsed.getInt("left");
            int right = parsed.getInt("right");
            return Map.of("left", left, "right", right);
        } catch (RuntimeException e) {
            throw new RuntimeException("Invalid mul input: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> div(String arguments) throws RuntimeException {
        try {
            ParsedCommand parsed = DIV.parse(arguments);
            double left = parsed.getDouble("left");
            double right = parsed.getDouble("right");
            return Map.of("left", left, "right", right);
        } catch (RuntimeException e) {
            throw new RuntimeException("Invalid div input: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> echo(String arguments) throws RuntimeException {
        try {
            ParsedCommand parsed = ECHO.parse(arguments);
            String message = parsed.getString("message");
            return Map.of("message", message);
        } catch (RuntimeException e) {
            throw new RuntimeException("Invalid echo input: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> search(String arguments) throws RuntimeException {
        try {
            ParsedCommand parsed = SEARCH.parse(arguments);
            String term = parsed.getString("term");
            boolean caseInsensitive = parsed.getBoolean("case-insensitive");
            return Map.of("term", term, "case-insensitive", caseInsensitive);
        } catch (RuntimeException e) {
            throw new RuntimeException("Invalid search input: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> dispatch(String arguments) throws RuntimeException {
        try {
            ParsedCommand parsed = DISPATCH.parse(arguments);
            String type = parsed.requireSubcommandName();
            return switch (type) {
                case "static" -> Map.of(
                        "type", type,
                        "value", parsed.getInt("value")
                );
                case "dynamic" -> Map.of(
                        "type", type,
                        "value", parsed.getString("value")
                );
                default -> throw new IllegalStateException("Unexpected dispatch subcommand '" + type + "'.");
            };
        } catch (RuntimeException e) {
            throw new RuntimeException("Invalid dispatch input: " + e.getMessage(), e);
        }
    }

}
