# PlayerRevive — Survivors United Fabric port

This branch targets **Fabric and Minecraft 1.21.11**. Install it on both clients and servers alongside Fabric API and CreativeCore Fabric 2.14.11+. Fabric Loader 0.19.5+ and Java 21+ are required; the release pipeline uses Java 25.

## Build and test

```sh
./gradlew build runGameTest
./gradlew runClientGameTest
```

The tests live in a separate mod and are not included in the playable JAR. The client test creates a world, downs a player, verifies state synchronization and blur, revives them using the selector command, then gives up over the network.

## Releases

Pull requests into `fabric-1.21.11` run the Fabric build and Minecraft tests. To publish after merging, create an immutable tag matching `mod_version`, replacing `+` with `-`: for example `fabric-v2.1.3-mc1.21.11`. The workflow publishes the playable JAR, sources and SHA-256 checksums only after all checks pass. Manual workflow runs build artifacts without publishing.

## Credits

Original mod: CreativeMD. Fabric 1.21.1 port: MikihinaSann. Fabric 1.21.11 maintenance: Survivors United. LGPL-3.0; see LICENSE. Original NeoForge branches remain available.

## Original upstream setup
https://github.com/CreativeMD/ForgeMods
