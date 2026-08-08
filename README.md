# MicroQuests

A Paper/Spigot plugin that runs short server-wide quest competitions: every
player races toward the same objective, and the first to finish wins the
reward.

Quests are generated from configurable pools of three types — kill a mob,
gather an item, or craft an item — with amounts rolled from configurable
ranges. Competitions start automatically when enough players are online,
show progress in the action bar, and expire unclaimed if nobody finishes in
time. Players who want no part of it can opt out for good.

Published on Spigot: <https://www.spigotmc.org/resources/124181/>

## Requirements

- Paper or Spigot 1.21+
- Java 17+

## Installation

Drop the jar into your server's `plugins` folder and restart. The plugin
writes `config.yml`, `messages.yml`, and `quests.yml` into
`plugins/MicroQuests/` on first start.

## Commands

- `/quest status` — show the active competition and time remaining.
- `/quest optout` — toggle your participation.

## Configuration

`config.yml`:

- `min-players` — online players required before a competition starts.
- `max-quest-time` — seconds before an unfinished competition expires.
- `rewards.on-victory` — console commands run for the winner, with
  `{player}`, `{quest}`, and `{amount}` placeholders.
- `rewards.fallback` — XP, items, and potion effects granted when no
  victory commands are configured. Amounts accept a number or a `min-max`
  range rolled per win.

`quests.yml` holds the quest pools: the eligible mobs and items per quest
type and the amount range for each. Invalid entries are logged and skipped.

All player-facing text lives in `messages.yml` and supports `&` color codes.

## Permissions

- `microquests.update.notify` — receive update notifications (default: op).

## Building

The plugin depends on two libraries that install to your local Maven
repository:

```bash
git clone https://github.com/bradley-t-t/coreapi && mvn -f coreapi install
git clone https://github.com/bradley-t-t/updaterapi && mvn -f updaterapi install
```

Then build the plugin jar:

```bash
mvn package
```

The shaded jar lands in `target/`.
