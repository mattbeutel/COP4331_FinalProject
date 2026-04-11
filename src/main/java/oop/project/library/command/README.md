# Command System

Handles creation of command structures and multi-argument parsing.

## Development Notes

- `CommandSpec` uses a small builder API so scenarios can define commands declaratively.
- Positional and named arguments are modeled with the same `ValueSpec<T>` abstraction, differing only by kind and a few configuration fields.
- `ParsedCommand` provides typed getters and a `Map` view so the library can be ergonomic both for users and for the provided scenarios.
- Command tokenization is handled in the command layer instead of relying entirely on `BasicArgs`, because the PoC/MVP behavior needs bare flags like `--case-insensitive`.
- The parser already supports one-level subcommands, which made the `dispatch` scenario straightforward.

## PoC Design Analysis

### Individual Review (Command Lead)

Good:
- The builder API keeps command definitions readable.
- Named arguments, default values, flags, and subcommands fit into one coherent parser model.

Less good:
- There is not yet support for repeated arguments or varargs-style positionals.
- The current subcommand support is intentionally simple and may need refactoring for deeper nesting.

### Individual Review (Argument Lead)

Good:
- Command parsing reuses typed argument parsing cleanly.
- Validation errors are wrapped with the argument name, which improves usability.

Less good:
- There is no help/usage text generation yet.
- Alias and default handling works, but the API could be expanded to make those features more self-documenting.

### Team Review

- We agree that keeping command definitions declarative is a strong design choice.
- We are less certain about how far to push a single parser model before introducing specialized command-node types for advanced subcommands and defaults.
