# StormFront

Localized lightning storm simulation for Minecraft Forge 1.7.10, inspired by Bigger Lightning Storms and designed to coexist with Better Rain.

## Design

- Preserve vanilla dimension-wide rain and thunder lifecycle.
- Leave Better Rain in charge of rain visuals, sound, and its own weather effects; no coremod modifications.
- Simulate drifting server-side storm cells during vanilla thunderstorms, concentrating lightning near storm centres.
- Strike only in already-loaded chunks and suitable biomes; never load terrain solely to generate lightning.
- Keep storm state per dimension, persist it through world saves, and limit work per tick.
- Run on dedicated servers; Better Rain integration must be optional.

## Planned modules

- `StormManager`: formation, movement, and dissipation.
- `StormCell`: position, radius, intensity, and velocity.
- `LightningController`: loaded-chunk-aware strikes and rate limits.
- `StormWorldData`: persistent world state.
- `StormConfig`: tunable settings.

## Status

Architecture documented; implementation and compatibility testing remain to be done.
