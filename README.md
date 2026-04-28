# Typing Race Simulator (ECS414U)

Java coursework project with:
- **Part I:** textual typing race simulation (`Part1/`)
- **Part II:** Swing GUI typing race simulation (`Part2/`)
- **Part III:** Git workflow (branching + meaningful commit history)

## Project Structure

```text
TypingRaceSimulator/
├── .git/
├── Part1/
├── Part2/
└── README.md
```

## Dependencies

- JDK 11+ (tested with standard `javac` / `java`)
- No third-party libraries required
- Swing is part of the standard JDK

## Setup

From repository root:

```bash
git clone https://github.com/andii-d/TypingRaceSimulator.git
cd TypingRaceSimulator
```

## Part I (Textual Version) — Compile & Run

### Compile

```bash
javac Part1/*.java
```

### Run

`TypingRace` does not ship with a fixed CLI `main` entry point. Start the race by constructing a `TypingRace` object and calling `startRace()` (for example from a temporary runner class, IDE runner, or a local `main` method), e.g.:

```java
TypingRace race = new TypingRace(40);
race.addTypist(new Typist('①', "TURBOFINGERS", 0.85), 1);
race.addTypist(new Typist('②', "QWERTY_QUEEN", 0.60), 2);
race.addTypist(new Typist('③', "HUNT_N_PECK", 0.30), 3);
race.startRace();
```

## Part II (GUI Version) — Compile & Run

### Compile

```bash
javac Part2/*.java
```

### Run

```bash
java -cp Part2 TypingRaceGUI
```

## Usage Guidelines (Spec-Aligned)

### Part I
- Uses `Typist` and `TypingRace` core simulation mechanics:
  - correct keystrokes progress forward
  - mistypes slide backward
  - burnout freezes typing temporarily
  - winner receives a small post-race accuracy adjustment

### Part II
- Pre-race setup:
  - passage selection (short/medium/long/custom)
  - seat count (2–6)
  - global modifiers (Autocorrect, Caffeine Mode, Night Shift, Rank Impact)
- Typist customisation:
  - typing style, keyboard type
  - symbol/emoji and color
  - accessories (wrist support, energy drink, noise-cancelling headphones)
- Race visualisation:
  - per-typist animated lane
  - passage progress highlighting
  - pause/resume controls
- Post-race analytics:
  - WPM, accuracy %, burnout count, accuracy change
- Reward systems:
  - **Option A:**
    - cumulative points leaderboard
    - rank-based comparison
    - race history per typist
    - milestone badges
  - **Option B:**
    - sponsor deals selected during setup
    - post-race earnings algorithm (placement, performance, penalties, sponsor bonus)
    - cumulative financial leaderboard
    - spendable upgrades (accuracy and burnout recovery improvements for future races)

## Git Requirements (Part III)

- Keep `main` stable.
- Implement GUI work on `gui-development`.
- Use descriptive commit messages that explain *what* changed and *why*.
- Merge `gui-development` back into `main` after GUI completion.
