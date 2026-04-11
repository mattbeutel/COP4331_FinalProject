# Argument System

Handles creation of creation command structures and multi-argument parsing.

## Development Notes

TODO: Keep a running log of design decisions, tradeoffs, and other observations.

## PoC Design Analysis

### Individual Review (Command Lead)

### Individual Review (Argument Lead)

Good:
- Command parsing reuses typed argument parsing cleanly.
- Validation errors are wrapped with the argument name, which improves usability.

Less good:
- There is no help/usage text generation yet.
- Alias and default handling works, but the API could be expanded to make those features more self-documenting.

### Team Review
