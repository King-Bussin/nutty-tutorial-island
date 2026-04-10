# Nutty Tutorial Island

A DreamBot script that automatically completes Tutorial Island with human-like behavior.

## Features

- Full 65-step tutorial completion from character creation to Lumbridge teleport
- Ironman mode support with bank PIN entry
- Custom human-like mouse algorithm with variable speed, fatigue, and micro-corrections
- Gaussian timing distributions with jitter (no fixed delays)
- 8+ anti-ban idle behaviors on exponential cooldowns
- Stuck detection watchdog with automatic recovery
- Clean paint GUI showing progress, varp, and anti-ban stats

## Usage

1. Log into a new account on Tutorial Island (character creation screen)
2. Start the script
3. Select normal or ironman mode when prompted
4. If ironman, enter your desired bank PIN

## Requirements

- DreamBot client
- Java 11

## Building

Compile `src/Main.java` against the DreamBot `client.jar` and package with `manifest.txt` as the JAR manifest.
