# Shadow Slave

A Minecraft mod inspired by the web novel **Shadow Slave**. Awaken your **Soul Core**, harvest
energy from everything you kill, and rise through the ranks of the Nightmare — beginning with your
first descent into the **Forgotten Shore**.

| | |
|---|---|
| **Minecraft** | 1.20.1 |
| **Loader** | Forge 47.x (`47.3.0`) |
| **Java** | 17 |
| **Mod ID** | `shadowslave` |

---

## Features

### ◈ The Soul Core UI
A dedicated interface, drawn entirely in code (no textures required), showing your progression.

Open it two ways:
- **Inventory button** — open your inventory and click **`❖ Soul Core`** (above the inventory window).
- **Hotkey `K`** — opens it directly (rebindable under Controls → "Shadow Slave").

It displays:
- An **animated Soul Core orb** whose liquid fill rises with stored **Essence**, ringed in your rank's colour.
- The **Aether** bar (your regenerating skill energy).
- Your **Rank** and its health/damage multiplier.
- **Soul Core capacity** and Nether Stars absorbed.
- **Flaw** and **Aspect** — placeholders for now (`[ Locked ]` / `[ Dormant ]`).
- A green **`Essence gain ×N`** line when a debug multiplier is active.

### ⚡ Two Energies
- **Aether** — regenerates naturally over time; intended to power skills. Current value is tracked;
  the maximum scales with Soul Core upgrades.
- **Essence** — harvested from kills, accumulates inside the Soul Core, and is used to advance the
  *quantity* (capacity) of your energies. Shown in the UI as the orb's fill percentage.

### ☠ Killing feeds the Soul Core
Slaying creatures adds Essence as a percentage of your Soul Core's capacity:

| Target | Essence |
|---|---|
| Peaceful mob | **0.25%** |
| Hostile mob | **0.5%** |
| Iron Golem | **3%** |
| Warden | **20%** |
| Ender Dragon | **30%** |
| Wither | **40%** |

### ✦ Nether Star — expand the Core
Right-click a **Nether Star** to consume it and increase **both** your Soul Core capacity and your
Aether capacity by **×1** (capacity = base × (1 + upgrades)).

### ⬆ Ranks
Each rank multiplies your **max health and attack damage** (applied as real attribute modifiers):

| Rank | Multiplier | How it's earned |
|---|---|---|
| Mortal | ×1.00 | Default, before any awakening |
| **Sleeper** | ×1.20 | First time you enter the Forgotten Shore |
| **Awakened** | ×1.75 | When you return to the waking world |
| Master | ×2.50 | *(placeholder scaling)* |
| Saint | ×3.50 | *(placeholder scaling)* |
| Supreme | ×5.00 | *(placeholder scaling)* |
| Sacred | ×7.00 | *(placeholder scaling)* |
| Divine | ×10.00 | *(placeholder scaling)* |

### 🌙 Sleep & the descent
On waking from a bed there is a **10% chance** you feel *"extremely drowsy."* The **next** time you
sleep, the world dissolves into cold grey mist and you are dragged into the **Forgotten Shore** —
becoming a **Sleeper**. Find your way back to the Overworld to **Awaken**.

### 🏝 The Forgotten Shore (custom dimension)
The first Nightmare a Sleeper endures — a desolate, fog-drowned coast modelled on the novel. It is
generated with custom blocks and worldgen code, not just vanilla pieces:

- **The Tide** — the sea swells with the dark and withdraws with the light. The waterline rises
  toward midnight (Y 70) and recedes by noon (Y 60), tracking the sun (`TideManager`). High tide
  floods the low coast; low tide bares the sea floor.
- **The Coral Labyrinth** — crimson coral walls tangle the sea floor into a maze (world-aligned grid
  with random doorways). Submerged at high tide, it becomes a navigable labyrinth as the water recedes.
- **Sleeper Statues** — fields of petrified dreamers (custom `petrified_sleeper` block), some standing,
  many already crumbled. Arriving on the Shore raises a cluster of statues around you — the stone
  company you wake amongst.
- **Crimson Spires** — tall, tapering, faintly glowing landmarks (`crimson_spire_block`) looming over
  the coast.
- **Drifting ash & grey gloom** — white ash falls endlessly; fog, sky, water and foliage are
  desaturated. A day/night cycle drives the Tide, so nights are dark, flooded and deadly.
- **Nightmare Creatures** — zombies, skeletons, husks, strays, spiders, drowned, phantoms, endermen
  and witches roam in force. You cannot rest here (beds don't work).

> Dimension id: `shadowslave:forgotten_shore`. Terrain uses vanilla overworld noise generation with a
> single fixed Forgotten Shore biome, then custom features (`crimson_spire`, `sleeper_statue`,
> `coral_labyrinth`) carve in the landmarks.

### 🧱 Custom blocks
In the **Shadow Slave** creative tab (textures are procedurally generated):

| Block | Role |
|---|---|
| **Ashen Sand** | Grey shore sand / ash drifts |
| **Petrified Sleeper** | The stone statues of dreamers |
| **Crimson Spire** | Glowing crimson landmark stone |
| **Crimson Coral** | Walls of the sea-floor labyrinth |

### 🛠 Playtesting commands — `/soulcore`
All require permission level 2 (op). Every value command takes an optional trailing `[player]`
(defaults to you); changes sync to the UI instantly.

| Command | Effect |
|---|---|
| `/soulcore info [player]` | Print full Soul Core state |
| `/soulcore essence add\|set\|percent <n> [player]` | Modify stored Essence (`percent` adds % of capacity) |
| `/soulcore essence full [player]` | Fill the core |
| `/soulcore aether set <n> [player]` · `aether full` | Set/refill Aether |
| `/soulcore multiplier set <x> [player]` | Multiply Essence gained from kills (e.g. `50` for fast testing) |
| `/soulcore rank set <rank> [player]` · `rank next` | Set / advance rank (reapplies buffs) |
| `/soulcore upgrades set <n> [player]` | Set Nether-Star upgrade count |
| `/soulcore netherstar <n> [player]` | Apply n Nether-Star upgrades |
| `/soulcore drowsy <true\|false> [player]` | Toggle the drowsy flag (then sleep to test the descent) |
| `/soulcore shore [player]` | Teleport across to the Forgotten Shore (or back) |
| `/soulcore flaw <text> [player]` · `aspect <text>` | Set the Flaw/Aspect placeholders |
| `/soulcore reset [player]` | Reset everything to defaults |

---

## Building & running

> The `--project-cache-dir .gradle-cli` flag keeps the CLI build from colliding with the VS Code
> Gradle integration's own background build (they share `.gradle` otherwise).

```bash
# Compile
./gradlew compileJava --project-cache-dir .gradle-cli

# Launch the dev client
./gradlew runClient --project-cache-dir .gradle-cli

# Build the distributable jar  ->  build/libs/shadowslave-0.1.0.jar
./gradlew build --project-cache-dir .gradle-cli
```

To install in a normal game, drop `build/libs/shadowslave-0.1.0.jar` into the `mods/` folder of a
**Minecraft Forge 1.20.1** profile.

---

## Project layout

```
src/main/java/com/shadowslave/
  ShadowSlave.java            Mod entry point + content registration
  soul/                       SoulData (capability), Rank, RankBuffs, capability plumbing
  network/                    SimpleChannel + S2C sync packet (server -> client UI)
  client/                     Keybind, inventory button, client cache, Soul Core screen
    gui/SoulCoreScreen.java   The Soul Core UI
  event/                      Capability attach, kill/sleep/dimension/tick/tide handlers
  command/ModCommands.java    /soulcore playtesting commands
  registry/                   ModBlocks, ModCreativeTabs, ModFeatures (DeferredRegisters)
  world/
    ForgottenShore.java       Dimension key, teleport, awakening statues
    TideManager.java          The rising/receding Tide
    feature/                  CrimsonSpire, SleeperStatue, CoralLabyrinth features

src/main/resources/
  META-INF/mods.toml
  data/shadowslave/dimension/forgotten_shore.json
  data/shadowslave/dimension_type/forgotten_shore.json
  data/shadowslave/worldgen/biome/forgotten_shore.json
  data/shadowslave/worldgen/{configured_feature,placed_feature}/   spire · statue · coral
  assets/shadowslave/{blockstates,models,textures,lang}/           custom blocks + UI strings
```

Per-player state lives in a Forge capability and persists through death/respawn and dimension travel.

---

## Roadmap / not done yet
- Spend **Aether** on actual skills (the pool exists but isn't consumed yet).
- Real **Flaw** and **Aspect** systems (currently UI placeholders).
- Higher-rank progression (Master → Divine multipliers are placeholders).
- Richer Forgotten Shore terrain — bespoke heightmap/surface (ashen beaches), plus the Tower and the
  Great Tide event. The Tide, statues, spires and coral labyrinth are in; terrain shape is still
  vanilla-overworld-derived.
- Proper sculpted statue/spire models (currently full-cube blocks) and art for the Soul Core orb.
