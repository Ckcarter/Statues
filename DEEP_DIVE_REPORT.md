# Statues20 parity map

## Sculpting
The classic hammer searches above/below the clicked block for an identical pair and opens GuiSculpt. Statues20 follows the same flow and refuses bedrock, fluids, block entities, fire and circuit-style redstone blocks. Sculpting replaces both source blocks atomically with linked lower/upper statue halves and stores the complete modern BlockState.

## Pose math
The old normalized StatueParameters values are retained with the same defaults and NBT names. The renderer maps them with the original formulas: right arm X 0..-180 and Y 90..-45; left arm X 180..0, Y 270..135 and Z 180; both legs X 120..-120 with mirrored Y ranges; head X -45..45 and Y 45..-45; body A/B rotate the entire statue Y -45..45 and X -30..30. Bent-leg height compensation is retained.

## Material skins
The old ImageStatueBufferDownload converted skin luminance into an overlay blend against the source block texture. Statues20 recreates that algorithm on 64x64 modern skins and samples the source BlockState's baked sprite. Bedrock remains the sentinel for the Palette's full-colour painted skin.

## Equipment
The finished statue owns six one-item slots: head, chest, legs, feet, right/main hand and left/off hand. Armor uses modern Forge armor hooks, leather color/overlay and foil rendering. Held items inherit the posed arm transform and the old normalized hand-angle transform.

## Showcase
The display case is center + two linked side blocks, one inventory slot, open-user counting, chest-like open/close sound, a cubic lid animation and an item display renderer. The custom model uses the old ModelShowcase Techne dimensions (128x64 texture layout).

## Effects and resources
Sculpting uses break particles from the source state on both blocks. Hammer pose copy and Palette painting have dedicated Statues20 sound events modeled after the original event structure. New original-independent textures/audio are supplied for this remake.

## Intentional modernization
The E inventory key is consumed by Statues20 screens instead of closing them. Modern Mojang skin lookup is used instead of the obsolete skins.minecraft.net path. Complete BlockState serialization replaces numeric block ID + metadata.
