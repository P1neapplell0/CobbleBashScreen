# Changelog

## 1.0.4

- Redesigned the Training Simulator in an Ore UI-inspired style, with beveled panels, a green highlight for the active tab, and a new challenge confirmation dialog.
- Updated all 18 gym cards to show whether the gym is ready to challenge, can be unlocked, or requires a Training Disc. Each card shows its win count and marks gyms that have been cleared.
- Improved the gym list with smooth mouse-wheel scrolling and a draggable scrollbar.
- Reworked the Elite Four tab to show the four matchups, the unlock requirement, and a dedicated action button.
- Added a gym completion progress bar alongside the challenge count and current challenge status.
- Updated the English and Simplified Chinese interface text and added Traditional Chinese, Japanese, Korean, French, German, Spanish (Spain and Mexico), Brazilian Portuguese, and Russian.

## 0.1.2

- Updated compatibility to CobbleBash 0.1.4 and reused its native dialogue translation support.
- Removed the redundant dialogue Mixin and client dialogue resource pack.
- Kept the server data pack that maps trainer dialogue fields to CobbleBash translation keys.

## 0.1.1

- Added NeoForge player Attachment storage for Training Simulator progress.
- Preserved the public `TrainingSimulatorApi` and training simulator event API.
- Added server-side challenge progress queries and completion events.
- Added an automatically enabled server data pack that uses CobbleBash's native English and Simplified Chinese NPC dialogue translations.
- Preserved original CobbleBash trainer teams, battle data, registrations, and gameplay behavior.
