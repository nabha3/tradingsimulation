# Contributing to BullRun

This is a team project for a university course, so these guidelines are short.
The goal is to keep the code easy to read, because every team member has to be
able to explain all of it.

## Setup

You need JDK 21 or newer.

```bash
git clone https://github.com/nabha3/tradingsimulation.git
cd tradingsimulation
javac -d bin src/*.java src/gui/*.java src/model/*.java src/exceptions/*.java
java -cp bin App
```

## Workflow

1. Pull the latest `main` before you start: `git pull origin main`.
2. Create a branch for your work, for example `feature/price-chart` or `fix/holding-check`.
3. Make small changes. One feature or fix per commit.
4. Compile and run the project before you commit.
5. Open a pull request and ask a teammate to review it.

## Commit messages

Write the subject line as a short instruction in the present tense, starting with a verb, with no full stop at the end. Keep it under about 70 characters. If the change needs explaining, leave a blank line and add a few plain sentences about what changed and why.

Good examples:

- `Add Market class with random price updates`
- `Fix negative share count in Holding`
- `Rename short variable names in Trader for readability`
- `Update README with new screenshots`

Avoid:

- `changes`, `fixed stuff`, `final version`, `asdf`
- Several unrelated changes in one commit

Common starting verbs: Add, Fix, Update, Remove, Rename, Refactor, Document.

## Branch names

Use lowercase words joined by hyphens, with a prefix that says what kind of work it is:

- `feature/admin-panel`
- `fix/login-error-message`
- `docs/report-diagrams`

## Naming conventions (Java)

| What | Style | Example |
|---|---|---|
| Class, interface, exception | `PascalCase`, a noun | `Trader`, `MarketListener`, `InvalidQuantityException` |
| Method | `camelCase`, starts with a verb | `buy()`, `getPortfolioValue()`, `refreshTable()` |
| Variable, parameter, field | `camelCase`, a meaningful noun | `walletBalance`, `selectedSymbol` |
| Constant (`static final`) | `UPPER_SNAKE_CASE` | `UPDATE_INTERVAL_MS`, `MAX_CHANGE` |
| Package | all lowercase, one word | `model`, `gui`, `exceptions` |

More rules:

- Use full words. Write `stock`, `holding` and `transaction`, not `s`, `h` and `t`. The only short names we use are `i` for a simple loop counter and `e` for an exception or an event.
- Name booleans like a question: `isRunning`, `hasHoldings`.
- A class name says what the thing is. An interface that reacts to events ends with `Listener`.
- Custom exceptions end with `Exception` and say what went wrong, such as `InsufficientFundsException`.
- Test or demo classes end with `Demo`, for example `ConsoleDemo`.

## Code style

- Plain Java 21. Avoid advanced features that are hard to explain, such as streams, lambdas or reflection. Stick to what we learnt in class.
- Add a short comment at the top of every class saying what it is for.
- Keep fields private and expose them through getters.
- Use our custom exceptions for invalid actions instead of printing errors.
- Put classes in the right package: `model` for the data and logic, `gui` for the windows, `exceptions` for our own exceptions.
- Keep methods short. If a method needs a comment for every few lines, split it up.
- The GUI stays plain Swing with the default look.
