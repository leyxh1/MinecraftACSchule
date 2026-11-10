# Auswahl des Systems und Soll-Zustand

## Gewähltes System

Ein **Minecraft-Multiplayer-Server (Paper)**, der über LAN bzw. Internet erreichbar ist und
von beliebigen Clients genutzt wird. Laut Szenario X ist das ein „Netzwerk aus einem
Schulprojekt“ bzw. ein „anderes vernetztes System“.

## Warum ist das System komplex genug? (Gesprächsgrundlage für das Lehrerteam)

- **Mehrere Komponenten:** Server-Betriebssystem, Java-Laufzeit, Server-Software, Plugins, Netzwerk (Router/Firewall, Portfreigabe), Clients.
- **Client-Server-Kommunikation über ein Netzwerkprotokoll** (TCP, Port 25565) mit nicht vertrauenswürdigen Gegenstellen.
- **Alle Schutzziele betroffen:** Integrität (Spielzustand), Verfügbarkeit (Server, Spieler), Vertraulichkeit (Spielerdaten, Admin-Zugang), Authentizität (Accounts).
- **Personenbezogene Daten** (Spielernamen, UUIDs, IP-Adressen) → rechtliche Anforderungen (DSGVO).
- **Eigene Entwicklung einer Schutzmaßnahme** (Anticheat in Java) statt nur Konfiguration.
- **Prüfbar:** Angriffe lassen sich mit einem öffentlich verfügbaren Cheat-Client live und reproduzierbar nachstellen.
- Übertragbares Prinzip: **„Never trust the client“** gilt genauso für Webshops, Apps und Online-Banking.

| Abstimmung mit dem Lehrerteam | |
|---|---|
| Datum | __.__.____ |
| Lehrkraft | |
| Ergebnis | ☐ Komplexität ausreichend ☐ Erweiterung nötig: … |

## IST-Zustand (kurz)

Ausführlich in [02-schutzbedarf.md](02-schutzbedarf.md#ist-zustand). Zusammengefasst: Ein frisch
aufgesetzter Paper-Server hat **keine Erkennung von Cheat-Clients**. Bewegungen und Angriffe
werden vom Client gemeldet und nur grob geprüft.

## Soll-Zustand (Sicherheitskriterien)

Diese Kriterien legen wir **gemeinsam mit dem Lehrerteam fest**. Nach der Umsetzung prüfen wir
jedes einzeln in [05-pruefung-bewertung.md](05-pruefung-bewertung.md#soll-ist-vergleich).

| Nr. | Sicherheitskriterium (Soll) | Messbar durch | Schutzziel |
|---|---|---|---|
| S1 | Fly, Speed, Reach, KillAura/Autoclicker und NoFall eines Cheat-Clients werden erkannt | Testfälle T1–T5: mind. **4 von 5** Cheats führen zu einer Warnung | Integrität |
| S2 | Wiederholte Verstöße führen automatisch zum Kick | Kick ab Grenzwert aus `config.yml` | Integrität, Verfügbarkeit |
| S3 | Normales Spielen löst **keinen Kick** aus | Testfälle F1–F8: **0 Kicks**, höchstens vereinzelte Warnungen | Verfügbarkeit |
| S4 | Admins werden bei Verstößen sofort informiert | Chat-Warnung an Spieler mit Berechtigung `anticheat.alerts` | Integrität |
| S5 | Verstöße sind nachvollziehbar protokolliert | Logdatei mit Zeit, Spieler, Check, Violation-Level | Nachvollziehbarkeit |
| S6 | Protokollierung ist datensparsam (DSGVO) | Logdatei enthält **keine IP-Adressen** | Vertraulichkeit, Recht |
| S7 | Grenzwerte sind ohne Programmierung anpassbar | Änderung in `config.yml` + Reload | Betrieb |
| S8 | Nur echte Mojang-Accounts können sich verbinden | `online-mode=true` | Authentizität |
| S9 | Nur der nötige Port ist von außen erreichbar, Fernwartung (RCON) ist aus | Firewall-Regel nur TCP 25565, `enable-rcon=false` | Vertraulichkeit, Verfügbarkeit |
| S10 | Code-Änderungen nur nach Vier-Augen-Prinzip | Branch-Schutz: PR + 1 Approval + grüner Build | Integrität |

| Abstimmung Soll-Zustand | |
|---|---|
| Datum | __.__.____ |
| Änderungen durch Lehrerteam | |
