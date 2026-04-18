# Command System

Handles creation of command structures and multi-argument parsing.

## Development Notes

- `CommandSpec` uses a builder so scenario definitions stay declarative and readable.
- Positional and named arguments share the same `ValueSpec<T>` abstraction, which keeps the command model small.
- `ParsedCommand` stores parsed state separately from command structure and exposes typed getters so incorrect extraction fails clearly.
- The builder validates preconditions such as duplicate names, aliases, and subcommand tokens when commands are created.
- Subcommand parsing returns the selected subcommand name along with the parsed values, which keeps dispatch logic in the scenario typed and explicit.

## MVP Design Analysis

### Individual Review (Command Lead)

Good:
- The builder-based API makes command definitions concise while still enforcing important invariants.
- Defaults, aliases, flags, and subcommands fit into one coherent parser model.

Less good:
- The current subcommand support is intentionally shallow and may need a richer representation if deeper nesting is added later.
- Repeated arguments and list-valued arguments are not modeled yet.

### Individual Review (Argument Lead)

Good:
- Command parsing reuses typed argument parsing cleanly instead of duplicating conversion logic.
- Typed extraction through `ParsedCommand` gives clearer failure modes than passing raw `Object` values around.

Less good:
- There is no generated help/usage layer yet, so the API is strong for parsing but still thin on user guidance.
- Tokenization currently lives inside the command system, which is practical for MVP behavior but could be split further if the input layer grows.

### Team Review

- We agree that validating command structure eagerly in the builder improves library correctness.
- We are unsure whether future expansion should continue adding features to `CommandSpec`, or split advanced subcommands into separate node types.
