# Create 6 — Fabric 1.21.1

![Create](.idea/icon.png)

Create adds mechanical components, moving contraptions and tools for building and automation.
This fork targets Minecraft 1.21.1 / Fabric and is based on the `mc1.21.1/fabric/dev`
branch of [Fabricators of Create](https://github.com/Fabricators-of-Create/Create).
The original mod is developed by [Creators of Create](https://github.com/Creators-of-Create/Create).
Blockfield maintains the changes in this repository.

## Build and checks

Requires JDK 21 (`JAVA_HOME`), Python 3.12+, Node.js 22 and Just 1.57.0.
Tools are cached inside the project; the commands work on Linux and Windows.

```sh
just setup
just check
just build
```

`just format` applies formatting. The mod JAR is written to `build/libs/`.

## Releases

After committing to `main`, run `scripts/bump-fork.sh create` from
[blockfield-client](https://github.com/Blockfield/blockfield-client). It creates a
`bfN` tag, waits for the build and pins the released JAR. Passing an existing `bfN`
as the second argument only updates the pin. Do not change the mod version by hand.
Shared mods also need the corresponding server pin and a coordinated server/client release.

## Upstream documentation

- [Modrinth](https://modrinth.com/mod/create-fabric) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-fabric)
- [Fabric addon template](https://github.com/Fabricators-of-Create/create-fabric-addon-template)
- [Multiloader addon template](https://github.com/Fabricators-of-Create/create-multiloader-addon-template)

Code is licensed under [MIT](LICENSE), as in the upstream project.
