# Schutzmaßnahmen

## IT-Sicherheitsleitlinie (für den Serverbetrieb)

Kurze Leitlinie, die der Betreiber für seinen Server festlegt:

1. **Fairness:** Auf dem Server sind nur unveränderte Clients erlaubt. Verstöße werden automatisch erkannt und können zum Kick oder Ausschluss führen.
2. **Never trust the client:** Spielrelevante Eingaben werden serverseitig auf Plausibilität geprüft.
3. **Minimale Rechte:** Admin-Rechte (OP) bekommen nur namentlich benannte Personen.
4. **Aktualität:** Server-Software und Plugins werden zeitnah aktualisiert und nur aus offiziellen Quellen bezogen.
5. **Datensicherung:** Die Spielwelt wird regelmäßig gesichert, die Wiederherstellung wird getestet.
6. **Datensparsamkeit:** Es werden nur die personenbezogenen Daten gespeichert, die für den Betrieb nötig sind.
7. **Nachvollziehbarkeit:** Sicherheitsrelevante Ereignisse werden protokolliert.

## Maßnahmenkatalog

### Technische Maßnahmen

| Maßnahme | gegen Risiko | Umsetzung |
|---|---|---|
| **Anticheat-Plugin** mit Fly-, Speed-, Reach-, KillAura- und NoFall-Check | R1–R5 | **Unser Projekt** (`src/`) |
| Violation-Level mit Grenzwerten statt Sofort-Kick | R11 | `config.yml` (Phase 3) |
| Admin-Warnungen und Logdatei | R1–R5, R7 | Plugin |
| `online-mode=true` (Mojang-Accounts prüfen) | Authentizität | `server.properties` |
| Firewall: nur TCP-Port 25565 freigeben, RCON/Query aus | R9, R10 | Windows-Firewall, Router |
| Regelmäßige Backups der Welt | R8 | Skript / Hoster |
| Updates von Paper und Plugins | R9 | Paper-Downloadseite |

### Organisatorische Maßnahmen

| Maßnahme | gegen Risiko |
|---|---|
| Serverregeln mit Cheat-Verbot, für alle sichtbar | R1–R6 |
| Rollen festlegen: wer ist Admin, wer Moderator | R7 |
| Admins prüfen Warnungen, bevor sie jemanden dauerhaft bannen (Mensch entscheidet) | R11 |
| Logdateien regelmäßig auswerten und nach Frist löschen | R12 |
| Vier-Augen-Prinzip bei Code-Änderungen (Pull Request + Review) | R9 |

## Rechtliche Regelungen

| Regelung | Bedeutung für uns | Umsetzung |
|---|---|---|
| **DSGVO Art. 5** (Datenminimierung, Speicherbegrenzung) | Nur nötige Daten speichern, nicht ewig aufbewahren | Logdatei enthält **Spielername, Check, Zeit** – **keine IP-Adressen**; Löschfrist festlegen |
| **DSGVO Art. 6 Abs. 1 lit. f** (berechtigtes Interesse) | Rechtsgrundlage für das Protokollieren von Verstößen | Fairer Spielbetrieb als berechtigtes Interesse |
| **DSGVO Art. 13** (Informationspflicht) | Spieler müssen wissen, dass protokolliert wird | Hinweis in Serverregeln / beim Betreten |
| **DSGVO Art. 22** (keine rein automatisierte Entscheidung mit erheblicher Wirkung) | Dauerhafte Sperren nicht nur durch den Algorithmus | Plugin **kickt** nur (Spieler kann wieder joinen); Bans entscheidet ein Admin |
| **Minecraft-EULA / Nutzungsrichtlinien** | Regeln für Serverbetreiber | Server nur mit eigener Lizenz, `eula=true` bewusst akzeptieren |
| **Urheberrecht / Lizenzen** | Fremde Software korrekt nutzen | Paper (GPL), unser Code (MIT), Cheat-Client nur heruntergeladen und nicht verändert |
| **Nutzung von Cheat-Clients** | Auf fremden Servern verstößt das gegen deren Regeln | Cheat-Client **nur auf unserem eigenen Testserver** mit eigenen Accounts |

## Entscheidung

Wir setzen um: **Anticheat-Plugin** (Hauptmaßnahme) und dokumentieren die Basismaßnahmen
(`online-mode`, Firewall, Backups, Rechte), die beim Testserver in Phase 2 konfiguriert werden.
X-Ray-Schutz und DDoS-Schutz akzeptieren bzw. übertragen wir bewusst (siehe Risikoanalyse).
