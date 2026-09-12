# Statues20 parity implementation notes

This Forge 1.20.1 rebuild follows the gameplay architecture of asiekierka/Statues rather than the earlier direct-placement prototype.

Implemented in this parity pass:
- Hammer-driven sculpting from two identical vertical source blocks, with server validation.
- Linked two-block statue with lower-half BlockEntity ownership.
- Player-name skin lookup and a live sculpt preview.
- The original normalized pose data and exact old pose equations for arms, legs, head, whole-body yaw/pitch, plus hand-item angles.
- Original six equipment slots: four armor and two hands, with armor/helmet and held-item rendering.
- Full source BlockState persistence.
- Dynamic player-skin/source-block texture overlay blending using the original luminance/overlay formula.
- Palette behavior using bedrock as the original painted/full-color-skin sentinel.
- Hammer copy-pose behavior for existing statues.
- Three-block-wide, one-slot Showcase with linked side blocks, modeled frame/base/cradle/lid, cubic lid animation, and displayed-item rendering.
- Sculpt, copy, and paint effects/sound events with replacement resources suitable for the 1.20.1 project.
- Dedicated Statues20 creative tab.
- E key is consumed by Statues20 screens so the inventory key does not close them.

Compatibility notes:
- Player skins are resolved through Mojang profile/session services; the default player skin is used while unavailable or unresolved.
- The original repository is GPL-2.0 and this project is distributed under GPL-2.0-only with attribution in README.md.
- GUI/audio/item resources in this project are replacement assets; the old source behavior/layout was used as the parity reference.
