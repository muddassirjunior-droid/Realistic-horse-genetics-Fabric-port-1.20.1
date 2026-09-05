# Mod page drafts — Realistic Horse Genetics (Fabric)

Copy the relevant section into CurseForge / Modrinth when creating the project.
Both platforms render Markdown-flavored text on the project description page.

---

## Title

Realistic Horse Genetics (Fabric)

## Summary / tagline (short, one line — CurseForge "summary" / Modrinth "summary")

A Fabric port of Realistic Horse Genetics: biologically realistic coat color and stat genetics for horses, donkeys, and mules.

## Categories / tags

Mobs, Adventure, World Generation (CurseForge) · Adventure, Game Mechanics (Modrinth)

## Description (long form)

**This is an unofficial Fabric port of [Realistic Horse Genetics](https://www.curseforge.com/minecraft/mc-mods/realistic-horse-genetics) by [sekelsta](https://github.com/sekelsta), originally built for Forge/NeoForge.** All credit for the genetics design, coat color research, and original code goes to sekelsta. This port exists because the original mod has no Fabric release; the goal was a faithful 1:1 translation, not a redesign.

**Disclaimer:** this port was produced with AI (LLM) assistance, translating the original source to Fabric's APIs line by line and testing the result in a running client. It hasn't had the years of community testing the original mod has — please report anything that looks off.

This mod adds biologically correct genetics for horses, massively expanding the available coat colors and patterns, along with plausible genetics for speed, health, and jump height. Mendelian and non-Mendelian inheritance are used where applicable, including chromosome linkage based on published equine genetics research.

**Features:**
- Realistic, non-Mendelian coat color and pattern genetics (bay, chestnut, black, dun, gray, cream, champagne, silver, leopard complex, pinto patterns, and many more) with real genetic linkage
- Genes for speed, jump height, and health that affect a horse's actual stats
- Variable adult size, inherited and randomized within realistic bounds
- A gender system with pregnancy and live birth (configurable)
- Right-click a tamed horse with a book to get a Genetic Testing Book showing its test results and physical traits
- Fully configurable — disable genders, sizes, genetic stats, or any individual system independently
- Supports horses, donkeys, and mules (including hinnies)

**What's different from the original Forge/NeoForge version:**
Gameplay and content are unchanged. A handful of things needed different plumbing because Fabric doesn't expose the same modding hooks Forge/NeoForge do (a custom brewing recipe, the horse inventory screen, and biome spawn placement all needed Fabric-native equivalents) — see the [GitHub repo](https://github.com/muddassirjunior-droid/Realistic-horse-genetics-Fabric-port-1.20.1) for the full technical writeup. The optional vanilla-horse-to-genetic-horse auto-conversion (off by default) covers normal spawning but not world-generation placement specifically.

**License:** LGPL-3.0-or-later (code) / CC-BY-4.0-or-later (assets), same as the original. Source: [GitHub repo](https://github.com/muddassirjunior-droid/Realistic-horse-genetics-Fabric-port-1.20.1)

**Requires:** Fabric Loader ≥0.15.0, Fabric API, Minecraft 1.20.1, Java 17+

## Changelog — 1.20.1-0.1.0 (initial release)

- Initial Fabric 1.20.1 port of Realistic Horse Genetics
- Full genetics engine, breeding, coat color/pattern rendering, and gene book ported from the original mod
- Custom brewing recipe, horse inventory screen, and biome spawn placement reimplemented for Fabric where no direct equivalent existed
