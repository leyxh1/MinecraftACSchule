# Informationssicherheit, IST-Zustand und Schutzbedarf

## Quellen und Standards (BSI)

| Quelle | Wofür wir sie nutzen |
|---|---|
| **BSI-Standard 200-1** (Managementsysteme für Informationssicherheit) | Begriffe, Sicherheitsleitlinie |
| **BSI-Standard 200-2** (IT-Grundschutz-Methodik) | Vorgehen und Schutzbedarfskategorien |
| **BSI-Standard 200-3** (Risikoanalyse auf Basis von IT-Grundschutz) | Vorgehen der Risikoanalyse |
| **IT-Grundschutz-Kompendium**, z. B. Bausteine *SYS.1.1 Allgemeiner Server*, *OPS.1.1.3 Patch- und Änderungsmanagement*, *OPS.1.1.5 Protokollierung*, *CON.2 Datenschutz*, *CON.3 Datensicherungskonzept*, *NET.3.2 Firewall* | Ideen für Maßnahmen in [04-massnahmen.md](04-massnahmen.md) |
| OWASP-Grundsatz „Never trust user input“ | Prinzip hinter serverseitigen Checks |

Alles unter [bsi.bund.de](https://www.bsi.bund.de) → IT-Grundschutz. Bausteinnamen vor der
Abgabe mit der aktuellen Kompendium-Edition abgleichen.

## Schutzziele der Informationssicherheit – übertragen auf den Server

| Schutzziel | Bedeutung allgemein | Bedeutung auf unserem Server | Bedrohung durch Cheats |
|---|---|---|---|
| **Vertraulichkeit** | Nur Berechtigte sehen Daten | Spielerdaten, Admin-Zugänge, Logdateien | X-Ray/ESP zeigt versteckte Infos (nicht Teil unseres Projekts) |
| **Integrität** | Daten sind korrekt und unverändert | Der Spielzustand (Positionen, Schaden, Leben) folgt den Spielregeln | **Hauptproblem:** Fly, Speed, Reach, KillAura, NoFall verfälschen den Spielzustand |
| **Verfügbarkeit** | System ist nutzbar, wenn es gebraucht wird | Server läuft, ehrliche Spieler können spielen | Cheater vertreiben Spieler; Lag-Exploits/DDoS (nicht Teil unseres Projekts) |
| **Authentizität** | Absender ist echt | Spieler ist der, der er vorgibt zu sein (`online-mode=true`) | Gefälschte Accounts bei `online-mode=false` |

**Kernaussage:** Unser Anticheat schützt vor allem die **Integrität** des Spielzustands und
damit indirekt die **Verfügbarkeit** des Angebots für ehrliche Spieler.

## IST-Zustand

Aufnahme des Testservers **vor** unseren Maßnahmen (in Phase 2 mit echten Werten ergänzen):

| Bereich | IST-Zustand | Problem |
|---|---|---|
| Server-Software | Paper (aktuelle Version), Standard-Konfiguration | Prüft Bewegungen nur grob („moved too quickly“), keine Kampf-Checks |
| Cheat-Erkennung | **keine** | Fly, Speed, Reach, KillAura, NoFall bleiben unbemerkt |
| Admin-Benachrichtigung | keine | Admins merken Cheats nur zufällig |
| Protokollierung | nur Standard-Serverlog (`logs/latest.log`, enthält IP-Adressen beim Login) | Keine Auswertung von Verstößen; IPs werden ohne Hinweis gespeichert |
| Authentifizierung | `online-mode=true` (Standard) | ✔ in Ordnung, nicht abschalten |
| Netzwerk | Port 25565 im LAN offen; ggf. Portfreigabe im Router | Prüfen: welche Ports sind wirklich offen? |
| Fernwartung | RCON standardmäßig aus | ✔ in Ordnung |
| Datensicherung | keine | Welt bei Defekt verloren |
| Updates | manuell | Gefahr veralteter Versionen |

## Schutzbedarfsfeststellung

Kategorien nach **BSI IT-Grundschutz (Standard 200-2)**:

| Kategorie | Bedeutung |
|---|---|
| **normal** | Schadensauswirkungen sind begrenzt und überschaubar |
| **hoch** | Schadensauswirkungen können beträchtlich sein |
| **sehr hoch** | Schadensauswirkungen können existenziell bedrohlich sein |

### Schutzobjekte

| Schutzobjekt | Vertraulichkeit | Integrität | Verfügbarkeit | Begründung |
|---|---|---|---|---|
| Spielzustand (Bewegung, Kampf) | normal | **hoch** | normal | Manipulation zerstört Fairness, Spieler verlassen den Server |
| Spielwelt (Bauwerke, Inventare) | normal | **hoch** | hoch | Viele Stunden Arbeit der Spieler, ohne Backup unwiederbringlich |
| Spielerdaten (Name, UUID, IP-Adresse) | **hoch** | normal | normal | Personenbezogene Daten → DSGVO |
| Anticheat-Logdatei | hoch | **hoch** | normal | Enthält Spielernamen; Grundlage für Kicks/Bans, darf nicht verfälscht werden |
| Admin-Zugang (OP-Rechte, Konsole) | **sehr hoch** | **sehr hoch** | hoch | Wer Admin ist, kontrolliert alles |
| Serverhardware / Netzwerk | normal | normal | hoch | Ausfall = Server offline |

**Ergebnis:** Für unser Projekt ist der Schutzbedarf der **Integrität des Spielzustands hoch**.
Daraus leiten wir ab, dass eine technische Schutzmaßnahme (Anticheat) nötig ist.
Die übrigen Objekte sichern wir mit Basismaßnahmen ab (siehe [04-massnahmen.md](04-massnahmen.md)).

> Wenn ihr ein Kundengespräch geführt habt: Antworten hier ergänzen und die Einstufung
> damit begründen.
