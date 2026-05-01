package oop.project.library.scenarios;

import oop.project.library.argument.ArgumentTypes;
import oop.project.library.argument.ParseException;
import oop.project.library.argument.Validators;
import oop.project.library.command.CommandSpec;
import oop.project.library.command.ParsedCommand;

import java.util.Map;

public final class CommandScenarios {

    public static Map<String, Object> mul(String arguments) throws RuntimeException {
        try {
            CommandSpec command = CommandSpec.builder("mul")
                    .addPositional("left", ArgumentTypes.integer())
                    .addPositional("right", ArgumentTypes.integer())
                    .build();
            ParsedCommand parsed = command.parse(arguments);
            int left = parsed.getInt("left");
            int right = parsed.getInt("right");
            return Map.of("left", left, "right", right);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid mul input: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> div(String arguments) throws RuntimeException {
        try {
            CommandSpec command = CommandSpec.builder("div")
                    .addNamed("left", ArgumentTypes.decimal())
                    .addNamed("right", ArgumentTypes.decimal())
                    .build();
            ParsedCommand parsed = command.parse(arguments);
            double left = parsed.getDouble("left");
            double right = parsed.getDouble("right");
            return Map.of("left", left, "right", right);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid div input: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> echo(String arguments) throws RuntimeException {
        try {
            CommandSpec command = CommandSpec.builder("echo")
                    .addOptionalPositional("message", ArgumentTypes.string(), "echo,echo,echo...")
                    .build();
            ParsedCommand parsed = command.parse(arguments);
            String message = parsed.getString("message");
            return Map.of("message", message);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid echo input: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> search(String arguments) throws RuntimeException {
        try {
            CommandSpec command = CommandSpec.builder("search")
                    .addPositional("term", ArgumentTypes.string())
                    .addFlag("case-insensitive", "i")
                    .build();
            ParsedCommand parsed = command.parse(arguments);
            String term = parsed.getString("term");
            boolean caseInsensitive = parsed.getBoolean("case-insensitive");
            return Map.of("term", term, "case-insensitive", caseInsensitive);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid search input: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> dispatch(String arguments) throws RuntimeException {
        try {
            CommandSpec command = CommandSpec.builder("dispatch")
                    .addSubcommand("static", CommandSpec.builder("static")
                            .addPositional("value", ArgumentTypes.integer())
                            .build())
                    .addSubcommand("dynamic", CommandSpec.builder("dynamic")
                            .addPositional("value", ArgumentTypes.string())
                            .build())
                    .build();
            ParsedCommand parsed = command.parse(arguments);
            String type = parsed.subcommandNameOption()
                    .orElseThrow(() -> new ParseException("Dispatch command did not record a selected subcommand."));
            return switch (type) {
                case "static" -> Map.of("type", type, "value", parsed.getInt("value"));
                case "dynamic" -> Map.of("type", type, "value", parsed.getString("value"));
                default -> throw new ParseException("Unexpected dispatch subcommand '" + type + "'.");
            };
        } catch (ParseException e) {
            throw new RuntimeException("Invalid dispatch input: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> report(String arguments) throws RuntimeException {
        try {
            CommandSpec command = CommandSpec.builder("report")
                    .addPositional("topic", ArgumentTypes.string())
                    .addOptionalNamed("limit", ArgumentTypes.integer().validate(Validators.integerRange(1, 25)), 10)
                    .addFlag("verbose", "v")
                    .build();
            ParsedCommand parsed = command.parse(arguments);
            String topic = parsed.getString("topic");
            int limit = parsed.getInt("limit");
            boolean verbose = parsed.getBoolean("verbose");
            return Map.of("topic", topic, "limit", limit, "verbose", verbose);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid report input: " + e.getMessage(), e);
        }
    }

}
