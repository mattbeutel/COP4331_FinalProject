# Final Project Library - MLP Writeup

## Feature Showcase

### Argument showcase: `ticket`

Command:
- `ticket AAA-1234`

What it demonstrates:
- A custom argument parser that validates a regex-shaped token and converts it into a structured domain value.
- This goes beyond the required built-in primitive parsing without adding special cases to the command system.

Returned map:
- `{prefix=AAA, number=1234}`

### Command showcase: `report`

Commands:
- `report sales --limit=5 -v`
- `report sales --limit=5 --verbose`
- `report sales --limit=5`

What it demonstrates:
- Inline named assignment with `=` for named arguments.
- Alias-aware flag parsing for both `-v` and `--verbose` styles.
- Default named values when `--limit` is omitted.

Returned map:
- `{topic=sales, limit=5, verbose=true}` when verbose is enabled.
