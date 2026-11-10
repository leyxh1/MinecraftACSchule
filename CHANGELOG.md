# Changelog

Alle wichtigen Änderungen an diesem Projekt werden hier dokumentiert.

Das Format basiert auf [Keep a Changelog](https://keepachangelog.com/de/1.1.0/),
und das Projekt folgt [Semantic Versioning](https://semver.org/lang/de/).

## [Unreleased]

### Added
- Checks: Fly, Speed, NoFall, Reach, KillAura, AutoClicker.
- ViolationManager: VL pro Spieler und Check, Admin-Meldungen, `violations.log` ohne IPs, Auto-Kick, VL-Abbau.
- Befehl `/ac status|alerts|reload`, alle Grenzwerte in `config.yml`.
- Doku: `docs/checks.md` (inkl. Grenzwerte einstellen), `docs/architecture.md`.

### Changed
- `start.bat` kopiert das Plugin bei jedem Neustart, damit ein neuer Build sofort aktiv ist.

### Added (Phase 1/2)
- Projektgerüst: Gradle-Build, Paper-Plugin-Grundklasse, GitHub-Actions-Build.
- Dokumentation (README, CONTRIBUTING, docs/).
- Testserver: `start.bat` mit automatischem Paper-Download (Prüfsumme), Plugin-Kopie, EULA-Abfrage und Neustart nach Absturz; gehärtete `server.properties`-Vorlage; Anleitung `docs/testserver.md`.
- Lernfeld-11-Dokumentation (Szenario X): Soll-Zustand, Schutzbedarf, IST-Zustand, Risikoanalyse, Maßnahmen, Prüfplan, Berichtsvorlage.

## [0.1.0] - TBD

### Added
- Erstes Release mit lauffähigem (noch leerem) Plugin.

[Unreleased]: https://github.com/leyxh1/MinecraftACSchule/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/leyxh1/MinecraftACSchule/releases/tag/v0.1.0
