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

- Höchstwert bleibt stehen (`violations.lock-at-max`): erreichte `kick-vl` werden in `/ac status` rot mit `MAX` angezeigt und nicht mehr abgebaut.
- Replays: Dashcam-Aufnahme der letzten 30 s jedes Spielers; beim Kick gespeichert in `replays/`. `/ac replay <Nr>` spielt sie mit einer Spielerpuppe (Mannequin) im Zuschauermodus ab, inkl. Meldungen zur richtigen Zeit. [Replay]-Knopf in Kick-Meldung und `/ac kicks`.
- Kick-Log: `/ac kicks [Spieler]` zeigt die letzten Kicks mit Zeit, Check und VL; gespeichert in `kicks.log`. Kick-Meldung und Liste haben einen anklickbaren [Bannen]-Knopf.
- `/ac reset <Spieler>`; `/ac status` funktioniert auch für gekickte (offline) Spieler und zeigt Balken.

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
