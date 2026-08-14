# CobbleBash Screen

CobbleBash Screen is a NeoForge add-on for CobbleBash. It replaces the Training Simulator interaction with a compact, technology-themed screen without replacing CobbleBash classes, registrations, or resources.

## Features

- Separate tabs for all 18 gyms and the Elite Four
- Nine gym cards visible at once with smooth continuous scrolling
- Permanent per-player challenge unlocks using the original CobbleBash Training Discs
- Separate unlock and challenge confirmation interactions
- Elite Four access remains locked behind completion of all 18 gyms
- Per-gym successful clear counts and total challenges started
- Persistent player progress backed by a NeoForge data attachment
- Cobblemon GUI click and PC unlock sounds
- English and Simplified Chinese interface translations, including a Simplified Chinese translation for CobbleBash

## Requirements

- A compatible Minecraft and NeoForge installation
- CobbleBash
- CobbleBash's own required dependencies

The development build expects the local CobbleBash dependency and other dependency jars declared in `build.gradle`.

## Integration API

Other server-side mods can unlock challenges without consuming a disc through `TrainingSimulatorApi`:

```java
TrainingSimulatorApi.unlockGym(player, GymType.FIRE);
TrainingSimulatorApi.unlockAllGyms(player);

if (TrainingSimulatorApi.isEliteFourAvailable(player)) {
    TrainingSimulatorApi.unlockEliteFour(player);
}
```

`unlockEliteFour` always preserves CobbleBash's progression order and returns `false` until the player has completed all 18 gyms.

The same API exposes server-safe progress queries without requiring access to attachments or NBT:

```java
int clears = TrainingSimulatorApi.getGymClearCount(player, GymType.FIRE);
boolean completed = TrainingSimulatorApi.hasClearedGym(player, GymType.FIRE);
int uniqueGyms = TrainingSimulatorApi.getClearedGymCount(player);
int started = TrainingSimulatorApi.getChallengesStarted(player);
```

Other mods can subscribe to these events on `NeoForge.EVENT_BUS`:

- `TrainingSimulatorOpenEvent.Pre`, cancellable before the screen opens
- `TrainingSimulatorChallengeStartedEvent.Post`
- `TrainingSimulatorGymClearedEvent.Post`
- `TrainingSimulatorEliteFourCompletedEvent.Post`

## Compatibility Design

The add-on opens its menu through NeoForge's block interaction event and calls CobbleBash's public `GymCommand` entry methods. One narrowly scoped Mixin observes successful returns from CobbleBash's private gym completion method so the UI can record repeat clear counts; it does not cancel or alter CobbleBash behavior.

Player UI progress is stored on each player in the serializable `cobblebash_screen:training_simulator` NeoForge attachment. The attachment is copied when the player respawns after death. Screen values remain server-authoritative and are synchronized through the menu rather than exposing the attachment to clients.

The add-on owns the `cobblebash_screen` registry namespace. Its `assets/cobblebash` resources only provide missing Simplified Chinese translations for CobbleBash and do not replace CobbleBash registrations or gameplay data.

## Build

Use Java 21 and run:

```shell
./gradlew build
```

The output is a versioned JAR in `build/libs/`.

## Author

P1nero

## License

LGPL-3.0-only
