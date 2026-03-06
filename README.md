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


## Solution

`TerminalBuffer` stores terminal content in two parts:

* Screen - a fixed grid of width * height cells (editable, visible area).
* Scrollback - lines that scrolled off the top of the screen (read-only), it's capped by `scrollbackMax`.

Each cell stores:

* a character,
* visual attributes: foreground color, background color, style flags (bold/italic/underline).

buffer also tracks:

* cursor position (col, row),
* current attributes used for future edits.

---

### Model and main decisions

#### Cell and Attributes

* `Cell` = `(char, Attributes)`
* `Attributes` = `(foreground, background, styleMask)`
  Styles are stored as a bit mask

Empty character is a space `' '` with default attributes (`Cell.EMPTY`).

#### Screen and Scrollback

Both screen and scrollback are stored as `Deque<Cell[]>`:

* each row is a `Cell[]`, length=`width`
* `screen` always has exactly `height` rows
* `scrollback` grows up to `scrollbackMax` rows (oldest lines are dropped when max is reached)

A deque gives O(1) removeFirst()/addLast() for scrolling.

#### Cursor

Cursor is always clamped to screen bounds:

* `0 <= col < width`
* `0 <= row < height`

movement by N cells rejects negative values.

---

### Operations

#### `write(text)`

* Writes at the cursor position using current attributes
* Overwrites existing cells
* Wraps at line end to the next row
* If writing goes past the bottom row, screen scrolls up, top row goes to scrollback

#### `insert(text)`

* Inserts text at the cursor position and shifts existing content to the right
* Overflow from the right end ("spill") cascades into the next row
* Inserted cells use current attributes; shifted cells preserve their original attributes
* Cursor moves forward by the inserted text length and stays within bounds

#### Other

* `fillLine(ch)`: fills the current row with `ch` using current attributes (cursor does not move)
* `insertEmptyLineAtBottom()`: shifts screen up by one row (same as a forced scroll)
* `clearScreen()`: clears only the screen, keeps cursor and scrollback
* `clearAll()`: clears both screen and scrollback, keeps cursor

#### Content access

* `getLine()`/ `getCell()` / `getAttributes()`
* `getScreenAsString()` get screen as string or 
* `getAllAsString()` screen+scrollback as string

#### Bonus: resize

`resize(newWidth, newHeight)`:

* Height grow: adds empty rows at the bottom
* Height shrink: moves top rows to scrollback
* Width change: pads/trims all rows (screen and scrollback) on the right
* Cursor is clamped to the new bounds

---

## Trade-offs

* Deque for rows: \
  scrolling is efficient, but indexed access to a row is O(height) because it iterates through the structure. Terminal height is small, so this is acceptable.
  - A ring-buffer array could make row access O(1)

* Resize does not reflow text: \
  width changes only pad/trim rows; it does not move cut-off characters into the next line. This keeps behavior is deterministic (and easier to test), but is less terminal-like.
    - A future improvement would be true reflow

* Insert implementation uses temporary arrays: \
  it is simple, but not the most allocation-efficient approach. It can be optimized later if needed

---

## Possible improvements

* Wide characters / Unicode width: would require a width calculation similar to `wcwidth` and likely switching from `char` to Unicode code points

* More terminal control features: handling escape sequences(`\n`, `\r`), tab stops

* Performance improvements: replace deque row access with a ring buffer for O(1) indexing, reduce allocations in `insert()`
