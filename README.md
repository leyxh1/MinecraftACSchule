# SchoolAntiCheat

![Build](https://github.com/leyxh1/MinecraftACSchule/actions/workflows/build.yml/badge.svg)
![Version](https://img.shields.io/github/v/release/leyxh1/MinecraftACSchule?include_prereleases)
![License](https://img.shields.io/badge/license-MIT-green)
![Paper](https://img.shields.io/badge/Paper-26.x-blue)

Ein serverseitiges Anticheat-Plugin für Paper-Server – entstanden als Schulprojekt.
Es erkennt typische Cheats (Fly, Speed, Reach, KillAura/Autoclicker, NoFall), zählt
Violation-Level pro Spieler und Check, warnt Admins und kickt ab einem Grenzwert.

> **Status:** Projektgerüst (Phase 1). Die Checks folgen in Phase 3.

## Schulprojekt: Lernfeld 11 FISI – Szenario X

*Betrieb und Sicherheit vernetzter Systeme gewährleisten.* Wir sichern ein selbst gewähltes
vernetztes System ab: einen Minecraft-Server, dessen Clients nicht vertrauenswürdig sind.

| Schritt | Dokument |
|---|---|
| System wählen, Soll-Zustand (Sicherheitskriterien) festlegen | [00 Auswahl & Soll-Zustand](docs/lf11b/00-auswahl-und-sollzustand.md) |
| Brainstorming / Mindmap Netzwerksicherheit | [01 Brainstorming](docs/lf11b/01-brainstorming.md) |
| Informationssicherheit (BSI), IST-Zustand, Schutzbedarf | [02 Schutzbedarf](docs/lf11b/02-schutzbedarf.md) |
| Risiken benennen und bewerten | [03 Risikoanalyse](docs/lf11b/03-risikoanalyse.md) |
| Maßnahmen, Sicherheitsleitlinie, rechtliche Regelungen | [04 Maßnahmen](docs/lf11b/04-massnahmen.md) |
| Exemplarische Umsetzung | Dieses Plugin (`src/`) + Testserver |
| Prüfung, Soll-Ist-Vergleich, Wirtschaftlichkeit | [05 Prüfung & Bewertung](docs/lf11b/05-pruefung-bewertung.md) |
| Dokumentation für das Lehrerteam | [06 Abschlussbericht](docs/lf11b/06-abschlussbericht.md) |

Übersicht: [docs/lf11b/README.md](docs/lf11b/README.md)

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
docs/lf11b/      Lernfeld-Doku: Schutzbedarf, Risikoanalyse, Maßnahmen, Bewertung
.github/         Workflows, Issue- und PR-Vorlagen
```

## Dokumentation
- [Lernfeld 11 – Sicherheitsanalyse und Bewertung](docs/lf11b/README.md)
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
