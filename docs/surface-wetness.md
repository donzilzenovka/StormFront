# Surface wetness — implementation investigation

Status: design only; not yet enabled in game.

## Verified exposure mechanism

Minecraft 1.7.10 rain splash generation queries `World.getPrecipitationHeight(x,z)`, checks biome precipitation and temperature, and spawns a splash on the surface immediately below that height. Reuse the height/biome logic, **not the particles themselves** (which depend on client particle settings).

## Lightweight plan

- Client-only and optional, disabled by default until a renderer integration is tested.
- Sample a bounded grid around the camera at low frequency; do not scan every block or generate chunks.
- Cache sampled surface positions, heights and wetness in a bounded, ephemeral client cache.
- Compute wetness from rainfall intensity and elapsed time, fading gradually after rainfall ends.
- Start with exposed horizontal upper faces; do not claim that vertical faces, cave entrances or overhangs are supported.
- Never alter world block metadata, textures on disk, server state or chunk lighting.
- Reuse Better Rain's strength only through an optional adapter; fall back to vanilla `World.getRainStrength`.
- Exclude snow, dry biomes, liquids, and transparent surfaces initially; configurable material multipliers later.
- Avoid a new coremod unless testing proves Forge's rendering hooks insufficient.

## Rendering decision still required

Exposure detection does not recolour rendered blocks. Before coding an effect, inspect Forge 1.7.10 RenderWorldLastEvent and block rendering hooks, plus Better Rain's access transformer, and measure a small overlay prototype. A naive translucent quad can cause z-fighting and bad results with slabs, stairs, and custom renderers; a shader or renderer hook may be preferable but has higher compatibility risk.

## Acceptance criteria

Wet surfaces visibly darken without flickering, fade after rain, respect roofs and biome rules, impose a small frame-time cost, and coexist with Better Rain and LWJGL3ify. No visual effect should be described as working until demonstrated in-game.
