Survivors United's Fabric port for Minecraft 1.21.11.

Required on clients and servers:
- Fabric Loader 0.19.5 or later
- Fabric API for Minecraft 1.21.11
- CreativeCore Fabric 2.14.11 or later for Minecraft 1.21.11
- Java 21 or later (our modpack uses Java 25)

Lethal damage starts a configurable bleeding-out phase. Hold right-click on a downed player to help them; releasing right-click cancels helping. Players can give up, and administrators can use `/revive @a[bleeding=true]`. Self-revive remains disabled by default. Enable `bleedInSingleplayer` to use the feature in singleplayer.

This release updates damage hooks, permissions, persistent player data and the downed HUD/blur for Minecraft 1.21.11. Downed state and consumed revive items survive saving/loading. Both dedicated-server game tests and a real Minecraft client test must pass before the workflow publishes a tag.

Based on CreativeMD's PlayerRevive and MikihinaSann's Fabric 1.21.1 port. Their history, attribution and LGPL-3.0 license are retained. The source JAR and checksums are included.
