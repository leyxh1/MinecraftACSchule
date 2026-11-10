# SchoolAntiCheat

![Build](https://github.com/leyxh1/MinecraftACSchule/actions/workflows/build.yml/badge.svg)
![Version](https://img.shields.io/github/v/release/leyxh1/MinecraftACSchule?include_prereleases)
![License](https://img.shields.io/badge/license-MIT-green)
![Paper](https://img.shields.io/badge/Paper-26.x-blue)

Ein serverseitiges Anticheat-Plugin für Paper-Server – entstanden als Schulprojekt.
Es erkennt typische Cheats (Fly, Speed, Reach, KillAura/Autoclicker, NoFall), zählt
Violation-Level pro Spieler und Check, warnt Admins und kickt ab einem Grenzwert.

> **Status:** Projektgerüst (Phase 1). Die Checks folgen in Phase 3.

## Features
- [ ] Fly-Check
- [ ] Speed-Check
- [ ] Reach-Check
- [ ] KillAura-/Autoclicker-Check
- [ ] NoFall-Check
- [ ] Violation-Level, Admin-Warnungen, Logdatei, Auto-Kick
- [ ] Grenzwerte in `config.yml`
- [ ] Befehl `/ac status <Spieler>`

## Voraussetzungen
- Java 25 (JDK)
- Paper-Server (aktuelle Version, siehe [docs/](docs/))

## Installation
1. Aktuelle `.jar` unter [Releases](../../releases) herunterladen.
2. In den Ordner `plugins/` des Paper-Servers legen.
3. Server starten – `plugins/SchoolAntiCheat/config.yml` wird erzeugt.

## Selbst bauen
```bash
./gradlew build
```
Die fertige Datei liegt danach in `build/libs/`.

## Nutzung
`/ac status <Spieler>` zeigt die aktuellen Violations (ab Phase 3).

## Projektstruktur
```
src/main/java/   Plugin-Code
src/main/resources/  plugin.yml, config.yml
docs/            Architektur, Checks, Git-Anleitung
.github/         Workflows, Issue- und PR-Vorlagen
```

## Dokumentation
- [Architektur](docs/architecture.md)
- [Beschreibung der Checks](docs/checks.md)
- [Git-Anleitung](docs/git-anleitung.md)
- [Aufgabenteilung](docs/aufgabenteilung.md)
- [Erstes Feature: kompletter Ablauf](docs/erstes-feature.md)

## Team
| Name | GitHub | Schwerpunkt |
|------|--------|-------------|
| Marcel | [@leyxh1](https://github.com/leyxh1) | siehe Aufgabenteilung |
| Niklas | [@salkinhopsongit](https://github.com/salkinhopsongit) | siehe Aufgabenteilung |

## Lizenz
[MIT](LICENSE)
