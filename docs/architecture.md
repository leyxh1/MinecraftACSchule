# Architektur (geplant)

```
AntiCheatPlugin            Einstiegspunkt, lädt Config, registriert Checks & Befehl
├── ViolationManager       zählt Violation-Level pro Spieler und Check, kickt, loggt
├── checks/
│   ├── Check              gemeinsame Basisklasse (Name, Grenzwerte, flag())
│   ├── FlyCheck
│   ├── SpeedCheck
│   ├── ReachCheck
│   ├── KillAuraCheck      (Klickrate + Blickwinkel)
│   └── NoFallCheck
└── commands/AcCommand     /ac status <Spieler>
```

## Ablauf
1. Ein Spieler bewegt sich oder schlägt zu → Paper löst ein Event aus (`PlayerMoveEvent`, `EntityDamageByEntityEvent`, ...).
2. Der passende Check prüft den Wert gegen den Grenzwert aus der `config.yml`.
3. Bei Verstoß ruft der Check `ViolationManager.flag(...)` auf.
4. Der Manager erhöht das Violation-Level, warnt Admins, schreibt ins Log und kickt ggf.

Die Details werden in Phase 3 ergänzt.
