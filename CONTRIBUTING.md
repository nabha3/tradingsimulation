# Contributing to BullRun

This is a team project for a university course, so these guidelines are short.
The goal is to keep the code easy to read, because every team member has to be
able to explain all of it.

## Setup

You need JDK 21 or newer.

```bash
git clone https://github.com/nabha3/tradingsimulation.git
cd tradingsimulation
javac -d bin src/App.java src/model/*.java src/exceptions/*.java
java -cp bin App
```

## Workflow

1. Pull the latest `main` before you start: `git pull origin main`.
2. Create a branch for your work, for example `feature/market-class` or `fix/holding-check`.
3. Make small changes. One feature or fix per commit.
4. Compile and run the project before you commit.
5. Open a pull request and ask a teammate to review it.

## Commit messages

Use a short sentence in the present tense that says what changed:

- `Add Market class with random price updates`
- `Fix negative share count in Holding`

## Code style

- Plain Java 21. Avoid advanced features that are hard to explain, such as heavy stream chains or reflection.
- Use clear names, and add a short comment to each class explaining what it is for.
- Keep fields private and expose them through getters.
- Use our custom exceptions for invalid actions instead of printing errors.
- Put model classes in `model` and exceptions in `exceptions`.
