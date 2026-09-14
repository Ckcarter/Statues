# Doenerstyle Statues 2.2.1 -> Statues20 1.20.1

Primary reference: Doenerstyle/Statues, version 2.2.1.

Fork-specific targets retained for the modern port:
- repaired player-skin retrieval behavior; modern Minecraft profile/skin APIs replace the legacy Minotar-only workaround
- Showcase GUI crash fix behavior
- Showcase lid must close correctly
- GUI strings are localization-ready
- improved localization-compatible UI behavior

Core classic behavior retained from the parity pass:
- hammer sculpts two matching stacked blocks
- linked two-block, life-sized player statue
- six 2D pose controls and original pose math
- source BlockState material appearance / painted palette mode
- armor and two hand slots with hand angles
- hammer pose copy
- three-wide animated Showcase with one stored/displayed item
- multiplayer NBT/network synchronization
- E key does not close Statues20 GUIs
