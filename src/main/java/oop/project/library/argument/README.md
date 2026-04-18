# Argument System

Handles parsing a single String input value into typed data.

## Development Notes

- `ArgumentType<T>` is the core polymorphic abstraction. New types are added by composition rather than by hardcoding special behavior into commands or scenarios.
- Validation is layered on top of parsing with `validate(...)`, which keeps concerns separate and makes ranges, choices, and regex checks reusable.
- `ParseFailure` is the standardized error type for parse and validation problems inside the library.
- `ArgumentTypes` contains built-in parsers for primitives and extensibility helpers for custom and enum-backed parsing.
- Enum parsing is case-insensitive so scenario inputs can be ergonomic while still producing strongly typed values.

## MVP Design Analysis

### Individual Review (Argument Lead)

Good:
- The library keeps parsing and validation separate, which makes the API easier to extend.
- Enum, regex, and custom parser support all fit into the same abstraction instead of requiring special-case scenario logic.

Less good:
- `ArgumentTypes` is convenient, but it centralizes many helpers into one utility class rather than a more discoverable package structure.
- The library currently focuses on parsing correctness and does not expose richer metadata such as usage/help text.

### Individual Review (Command Lead)

Good:
- The argument layer integrates cleanly into command definitions without leaking command-specific responsibilities into the parser abstraction.
- Standardized `ParseFailure` messages make it straightforward for commands to wrap failures with argument context.

Less good:
- Some advanced validation patterns, such as cross-field validation, are still outside the scope of the current design.
- We normalize enum casing for usability, but there is not yet a configurable casing policy for all parsers.

### Team Review

- We agree that keeping the argument layer independent from command structure was the right decision.
- We still see room to improve how custom types and validators are documented so the API is more self-explanatory for first-time users.
