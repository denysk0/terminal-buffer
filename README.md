# terminal-buffer

A **terminal text buffer** — the core data structure that terminal emulators use to store and manipulate displayed text.

## Requirements

- Java 21+ (compiled against Java 21 toolchain)
- Gradle 9.4.0 (wrapper included)

## Running tests

```bash
./gradlew test
```

## CI

GitHub Actions runs the test suite against Java 21 and Java 25 on every push
and pull request. See [`.github/workflows/ci.yml`](.github/workflows/ci.yml).
