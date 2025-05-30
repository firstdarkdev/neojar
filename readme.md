## NeoJar

---

A Custom JarInJar Loader for NeoForge 1.20.4.

This was made to allow mods that JIJ forge, fabric and neoforge mods into the same jar to load correctly.

### Why is this needed??

Since Forge and NeoForge both use `mods.toml` in 1.20.4, forge tries to load neoforge mods as forge mods, and crashes because it can't. So this wrapper is used to inject a custom jar-in-jar loader to allow neoforge to load its own mods, without forge catching a fit.

### Again, why??

Well, I wanted to pack some of my mods into the same jar, to make it easier for users to download, and less of a hassle for me to publish. I found out that JIJ from all 3 modloaders work perfectly for this, except when Forge and NeoForge is used on 1.20.4.

Used by [orion](https://github.com/firstdarkdev/orion) to package mods into a single jar. 