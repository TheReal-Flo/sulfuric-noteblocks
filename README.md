# Sulfuric Noteblocks

*What if a sulfur cube ate a note block?*

A tiny [Fabric](https://fabricmc.net/) mod for **Minecraft 26.2** that teaches the
Chaos Cubed sulfur cube a new trick: swallowing note blocks — and becoming a
bouncing, tunable, percussion instrument.

## What it does

Vanilla sulfur cubes refuse to eat utility blocks. This mod adds the note block
to their diet, and once one is inside, the cube **fully acts like a note block**:

### 🎵 Feed it
Right-click a large sulfur cube with a note block (or just drop one nearby —
it'll happily bounce over and slurp it up). The note block is a wooden block,
so the cube gets the **bouncy archetype**, same as planks and logs. Shear it
to get the note block back, just like vanilla.

### 🎹 Tune it
Right-click the cube with an **empty hand** to cycle through all 25 notes —
complete with the note particle floating up, a sound preview, and the vanilla
*"Note Blocks Tuned"* statistic. The preview instrument comes from the block
the cube is sitting on, exactly like a placed note block reads the block
beneath it. The tune is saved with the cube.

### 🥁 Launch it
Here's the fun part. Punch the cube (it plays its note as it flies, like
left-clicking a note block) and send it bouncing across the world. **Every
time it smacks into a block, it plays its note with that block's
instrument**:

| It hits...      | You hear...    |
|-----------------|----------------|
| Gold block      | Bells          |
| Bone block      | Xylophone      |
| Dirt or stone   | Bass drum      |
| Planks          | Bass           |
| Packed ice      | Chimes         |
| Wool            | Guitar         |
| A zombie head   | *Brains...*    |

Walls, floors, ceilings — every surface counts. One well-aimed whack down a
canyon becomes a melody.

### 🔴 Power it
Run a redstone signal into the cube's position and it plays its note on the
rising edge, so it slots straight into your note block contraptions — a
note block on wheels, if the wheels were goo.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) ≥ 0.19.3 for Minecraft 26.2
2. Drop [Fabric API](https://modrinth.com/mod/fabric-api) and this mod's jar into your `mods` folder
3. Find a sulfur cave, befriend a cube, hand it a note block

Works on servers too — all logic runs server-side, so vanilla clients on a
modded server hear the notes and see the particles. (Only cosmetic difference:
without the mod, a vanilla client's arm doesn't swing when tuning with an
empty hand.)

## Building from source

```
./gradlew build
```

The jar lands in `build/libs/`. Requires Java 25. The project uses the
post-26.1 toolchain (official Mojang mappings, no Yarn).

## How it works

- Two datapack tags opt `minecraft:note_block` into `#sulfur_cube_swallowable`
  and `#sulfur_cube_archetype/bouncy` — the cube's physics are fully
  data-driven in 26.2, so no code is needed for absorption.
- A mixin on `SulfurCube` handles tuning, the punch note, redstone edges, and
  saving the note.
- A mixin on `Entity#restituteMovementAfterCollisions` — the 26.2 bounce
  physics — catches the exact moment of impact while the pre-bounce velocity
  is still intact, works out which block was hit, and plays the note with that
  block's `NoteBlockInstrument`.

## License

MIT — see [LICENSE.txt](LICENSE.txt).
