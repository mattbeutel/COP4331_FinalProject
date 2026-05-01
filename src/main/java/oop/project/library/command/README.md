# Command System

Handles command structure and multi-argument parsing on top of the argument system.

## Development Notes

- `CommandSpec.Builder` now exposes explicit methods for positional, named, optional, flag, and subcommand arguments instead of requiring callers to build intermediate `ValueSpec` objects.
- Commands either have direct arguments or subcommands; mixing the two is rejected eagerly in the builder to reduce parser invariants.
- `ParsedCommand` is immutable and stores only parsed state. It provides typed extraction methods that throw `ParseException` when callers use the wrong name or type.
- `CommandSpec.parse(String)` now directly uses the provided `Input` system through `parseBasicArgs()` rather than duplicating tokenization logic in the command layer.
- The input layer was expanded to support bare flags and `--name=value` / `-name=value` inline assignments, which is used in the showcase scenario.

## Feature Showcase

- `report sales --limit=5 -v`
- `report sales --limit=5 --verbose`
- `report sales --limit=5`
    - Demonstrates inline named values with `=` plus alias-aware flags.
    - Returned map: `{topic=sales, limit=5, verbose=true}` when `-v` or `--verbose` is present.

## MLP Design Analysis

### Individual Review (Command Lead)

Good:
- The builder API now communicates command shape directly and avoids the earlier duplication between builder intent and `ValueSpec.Kind`.
- Eager validation prevents several malformed command definitions, including duplicate names, alias collisions, mixed subcommands/direct arguments, and required positionals after optional positionals.

Less good:
- The command model still only supports one level of subcommands.
- Named arguments are last-write-wins at the `Input` level, so the parser has to detect duplicate canonical arguments during resolution.

### Individual Review (Argument Lead)

Good:
- The command layer now depends on the input parser without reimplementing token handling, which clarifies the separation of concerns.
- Typed extraction remains straightforward for simple scenarios while still surfacing precise errors.

Less good:
- Builder validation is more eager now, but the implementation still maintains some temporary builder-side sets to detect conflicts.
- There is no generated help/usage text yet, so command definitions are expressive for parsing but not for user guidance.

### Team Review

- We agree that explicit builder methods made the command API more readable and approachable for simple use cases.
- We are still unsure whether future expansion should continue to grow one `CommandSpec` type or introduce distinct command-node variants for more advanced trees.
