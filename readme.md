## NeoJar

---

A Custom JarInJar Loader for NeoForge and Forge.

This was made to allow mods to JIJ forge, fabric and neoforge mods into the same jar and load correctly.

### Why is this needed??

Because I am lazy, and want to make things easier for my users, by offering them a single jar for all supported modloaders, instead of having a ton of separate jars.

Due to how forge and neoforge works, it can't just load an empty jar, with other jars inside it. This library enables both of these modloaders to load a dummy jar, that contains all the modloader jars inside it.

Used by [orion](https://github.com/firstdarkdev/orion) to package mods into a single jar. 