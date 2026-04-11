# Argument System

Handles parsing a single String input value into typed data.

## Development Notes

- `ArgumentType<T>` is the core abstraction. It keeps parsing logic generic while preserving static typing at the API boundary.
- Validation is composable through `validate(...)` instead of being hard-coded into each parser. That makes it easy to layer ranges, choices, and future regex/enum checks.
- Parse failures use a dedicated `ParseFailure` exception so command parsing can wrap errors with argument-specific context.
- Basic built-in types live in `ArgumentTypes` to keep scenario/command setup concise.
- The current design is intentionally small, but already leaves room for future expansion such as enums, regex-backed strings, and richer custom types.

## PoC Design Analysis

### Individual Review (Argument Lead)

Good:
- The argument API is small and consistent: every type is just an `ArgumentType<T>`.
- Validation is reusable and chainable, which keeps the parsing layer from becoming cluttered.

Less good:
- Error messages are readable, but not yet as rich as they could be for full debugging metadata.
- The current API is functional-first; a more discoverable builder-style API may be nicer for some users.

### Individual Review (Command Lead)

Good:
- The argument layer integrates cleanly with command definitions.
- Type-specific parsing is centralized instead of scattered through commands.

Less good:
- Some future command features, like defaults that depend on other args, are not modeled yet.
- Enum/regex support is planned but not demonstrated in the PoC scenarios.

### Team Review

- We like the separation between parsing a raw value and validating a parsed value.
- We are still unsure whether future functionality should keep extending `ArgumentType<T>` directly, or introduce a richer spec object around it for metadata/help text.
