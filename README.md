# StormFront

Forge 1.7.10 server-side localized lightning simulation, inspired by Bigger Lightning Storms. Designed to coexist with Better Rain 0.15 without touching its renderer, sound or coremod.

## Prototype 0.1.0

- Vanilla dimension-wide weather remains authoritative; cells only exist during thunderstorms.
- Up to three drifting storm cells are simulated in the Overworld by default.
- Each cell has a position, radius, velocity, lifetime and intensity ramp/decay.
- Lightning is attempted in loaded chunks only, in lightning-capable biomes, using vanilla weather entities.
- Storm cells persist via world saved data; the config file controls generation and lightning rate.
- All simulation runs on the logical server. Better Rain is optional, with no direct class linkage.

## Build

Uses RetroFuturaGradle 1.4.9, Java 8 toolchain, Minecraft 1.7.10. Run `gradle build` with a compatible Gradle installation. Gradle wrapper is not yet included.

## Testing status

**Unbuilt prototype:** compilation, dedicated-server startup, and Better Rain coexistence have not yet been verified. Please test in a disposable world before including it in the main modpack. Localized rain rendering is not implemented; the storm cells control additional lightning only.
