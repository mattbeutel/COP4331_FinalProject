# Final Project Library

A small Java library for building command-line style parsers with **typed argument parsing** and **structured command parsing**.

The project is split into two systems:
- **Argument system**: parses one raw `String` token into a typed value such as `int`, `double`, `boolean`, enum, or a custom domain type.
- **Command system**: describes command structure around those argument types, including positional arguments, named arguments, flags, defaults, and subcommands.

This README is a quick entry point for using the library. For lower-level details, see the JavaDoc comments and the system READMEs in:
- `src/main/java/oop/project/library/argument/README.md`
- `src/main/java/oop/project/library/command/README.md`

## Beginner's Guide

### 1. Start with built-in argument types

The simplest parsers come from `ArgumentTypes`:

```java
import oop.project.library.argument.ArgumentTypes;

ArgumentTypes.integer(); // parses int
ArgumentTypes.decimal(); // parses double
ArgumentTypes.bool();    // parses true / false
ArgumentTypes.string();  // raw string
```

You can also build custom parsers:

```java
import oop.project.library.argument.ArgumentType;
import oop.project.library.argument.ArgumentTypes;
import oop.project.library.argument.ParseException;

ArgumentType<Point> point = ArgumentTypes.custom("point", raw -> {
    String[] pieces = raw.split(",");
    if (pieces.length != 2) {
        throw new ParseException("Expected x,y but got '" + raw + "'.");
    }
    return new Point(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1]));
});
```

### 2. Add validation when needed

Parsers can be composed with reusable validators:

```java
import oop.project.library.argument.ArgumentType;
import oop.project.library.argument.ArgumentTypes;
import oop.project.library.argument.Validators;

ArgumentType<Integer> smallPositiveInt =
    ArgumentTypes.integer().validate(Validators.integerRange(1, 10));
```

Other common options include:
- `Validators.doubleRange(...)`
- `Validators.choices(...)`
- `Validators.regex(...)`

Enums are supported directly:

```java
enum Difficulty { PEACEFUL, EASY, NORMAL, HARD }

ArgumentType<Difficulty> difficulty = ArgumentTypes.enumType(Difficulty.class);
```

`enumType(...)` is case-insensitive, so inputs such as `easy`, `EASY`, and `Easy` all parse successfully.

### 3. Create a command

Commands are created with `CommandSpec.builder(...)`.

#### Positional arguments

```java
import oop.project.library.command.CommandSpec;

CommandSpec add = CommandSpec.builder("add")
    .addPositional("left", ArgumentTypes.integer())
    .addPositional("right", ArgumentTypes.integer())
    .build();
```

#### Named arguments

```java
CommandSpec div = CommandSpec.builder("div")
    .addNamed("left", ArgumentTypes.decimal())
    .addNamed("right", ArgumentTypes.decimal())
    .build();
```

This accepts input like:

```text
div --left 10.0 --right 2.0
```

#### Optional/default positional arguments

```java
CommandSpec echo = CommandSpec.builder("echo")
    .addOptionalPositional("message", ArgumentTypes.string(), "echo,echo,echo...")
    .build();
```

This accepts both:

```text
echo
```

and

```text
echo hello
```

#### Flags and aliases

```java
CommandSpec search = CommandSpec.builder("search")
    .addPositional("term", ArgumentTypes.string())
    .addFlag("case-insensitive", "i")
    .build();
```

This accepts:

```text
search apple --case-insensitive
search apple -i
```

#### Optional/default named arguments

```java
CommandSpec report = CommandSpec.builder("report")
    .addPositional("topic", ArgumentTypes.string())
    .addOptionalNamed("limit", ArgumentTypes.integer(), 10)
    .addFlag("verbose", "v")
    .build();
```

This accepts:

```text
report sales
report sales --limit 5
report sales --limit=5 -v
```

### 4. Parse and extract typed values

Parsing returns a `ParsedCommand`, which stores parsed state and exposes type-safe accessors.

```java
import oop.project.library.command.ParsedCommand;

ParsedCommand parsed = add.parse("1 2");
int left = parsed.getInt("left");
int right = parsed.getInt("right");
```

Generic extraction is also available:

```java
Difficulty difficultyValue = parsed.get("difficulty", Difficulty.class);
```

If the name is wrong or the expected type is wrong, the library throws `ParseException` with a descriptive error.

### 5. Use subcommands for different command shapes

```java
CommandSpec dispatch = CommandSpec.builder("dispatch")
    .addSubcommand("static", CommandSpec.builder("static")
        .addPositional("value", ArgumentTypes.integer())
        .build())
    .addSubcommand("dynamic", CommandSpec.builder("dynamic")
        .addPositional("value", ArgumentTypes.string())
        .build())
    .build();
```

This accepts different structures depending on the selected subcommand:

```text
dispatch static 1
dispatch dynamic hello
```

You can inspect the selected subcommand with:

```java
ParsedCommand parsed = dispatch.parse("static 1");
String type = parsed.subcommandNameOption().orElseThrow();
```

## Common Example

Here is a complete example using several common features together:

```java
import oop.project.library.argument.ArgumentTypes;
import oop.project.library.argument.ParseException;
import oop.project.library.argument.Validators;
import oop.project.library.command.CommandSpec;
import oop.project.library.command.ParsedCommand;

CommandSpec report = CommandSpec.builder("report")
    .addPositional("topic", ArgumentTypes.string())
    .addOptionalNamed("limit", ArgumentTypes.integer().validate(Validators.integerRange(1, 25)), 10)
    .addFlag("verbose", "v")
    .build();

try {
    ParsedCommand parsed = report.parse("sales --limit=5 -v");
    String topic = parsed.getString("topic");
    int limit = parsed.getInt("limit");
    boolean verbose = parsed.getBoolean("verbose");
} catch (ParseException e) {
    // turn library failure into your user-facing message
}
```

## Feature Showcase

These are the two showcase examples that go beyond the baseline assignment requirements.

### Argument showcase: structured custom parsing with `ticket`

```text
ticket AAA-1234
```

What it demonstrates:
- A custom argument parser can do more than convert to primitives: it can validate a token and return a structured domain value.
- The command system does not need special logic for this feature; it just consumes an `ArgumentType<T>` like any other argument.

In the scenario, the raw token is parsed into a `TicketCode(prefix, number)` record and then returned as:

```text
{prefix=AAA, number=1234}
```

Why this is useful:
- It keeps domain-specific parsing in user code rather than bloating the library with one-off types.
- It follows the same API as built-in argument types, so advanced parsing does not require a second system.

### Command showcase: inline named assignment with `report`

```text
report sales --limit=5 -v
report sales --limit=5 --verbose
report sales
```

What it demonstrates:
- Named arguments can be written as `--name value` or `--name=value`.
- Flags can have aliases such as `-v` and `--verbose`.
- Optional named arguments and flags still produce typed parsed values with defaults.

Returned values include:

```text
{topic=sales, limit=5, verbose=true}
```

Why this is useful:
- It reduces boilerplate for users entering commands.
- It is especially handy for flags and short named options, where compact syntax is nicer to type.

## Notes on Error Handling

The library uses `ParseException` for parse and extraction failures.
- Library code throws `ParseException`.
- Scenario methods catch `ParseException` and convert it to a `RuntimeException` with scenario-specific context, because the provided scenario API expects that behavior.

This keeps error handling standardized inside the library while still matching the assignment harness at the boundary.
