# Contributing to Create

Create is always looking for help. There's limited developers, limited time, and unlimited problems.
All contributions are welcome! Here's a quick guide on how you can help if you're interested:

### Translating

Translating Create into other languages makes it more accessible to those who don't speak english.
If you speak multiple languages, maybe give translating a shot!

Language contributions should generally be submitted to the [Forge repo](https://github.com/Creators-of-Create/Create).
They'll get pulled in after a while. However, this port also has some additional strings needing translating, so keep that in mind.

To get started, see the
[Localization Readme](https://github.com/Fabricators-of-Create/Create/blob/mc1.18/fabric/dev/src/main/resources/assets/create/lang/Localization%20Readme.txt).

### Issues

Nothing is perfect, not even Create.

[The issue tracker](https://github.com/Fabricators-of-Create/Create/issues) is full of things needing fixing.
Oversights, inconsistencies, bugs, and crashes. Any contributions that can close issues are greatly appreciated.

If nothing there seems doable or interesting, maybe take a look at the
[Forge repo's issues](https://github.com/Creators-of-Create/Create/issues).
Nearly all bugs in the Forge version are inherited, and upstream fixes will be pulled in as updates come.

There's some guidelines that should be followed when contributing code to Create. Keep reading for details.

### Code Guidelines

Thank you for wanting to contribute to Create! We have some rules though.

1.  **Minimize Changes**

    Changes should usually be as concise and minimally-invasive as possible.
    This is done to maintain upstream compatibility. We aim to stay up to date
    with the Forge version, pulling in updates as they come. Keeping the diff
    minimal makes merging feasible and is required for
    long-term maintenance.

2.  **Keep Code Clean & High Quality**

        Follow the coding conventions in place. They're standard for the most part,

    and not hard to follow. The project has an editorconfig file to help as well.
    "High Quality" is subjective, and will be case-by-case. Avoid bad practices and
    overcomplicating things. The most important bad practice to avoid is using
    Fabric implementation details, as this will probably make the mod crash on Quilt.

3.  **Utilize Porting Lib**

        [Porting Lib](https://github.com/Fabricators-of-Create/Porting-Lib) powers this port.

    The idea is to split APIs and such into a library mod, so other mods can use them
    as well. Porting Lib holds nearly all the Forge → Fabric replacements and bridges, with
    the only exceptions being super Create-specific stuff. Adding a replacement API
    or bridging something? You should probably head there.

4.  **Ask Questions**

        Not sure about something? Want feedback? Talk to us either on GitHub or through

    the [official Discord server](https://discord.gg/hmaD7Se) in #devchat.
