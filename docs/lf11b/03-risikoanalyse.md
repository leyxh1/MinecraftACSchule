# Risikoanalyse

**Risiko = Eintrittswahrscheinlichkeit × Schadenshöhe**

| Wert | Eintrittswahrscheinlichkeit | Schadenshöhe |
|---|---|---|
| 1 | selten (kaum zu erwarten) | gering (kaum spürbar) |
| 2 | möglich (ab und zu) | mittel (ärgerlich, behebbar) |
| 3 | häufig (regelmäßig) | hoch (Spieler wandern ab, Datenverlust) |

Bewertung: **1–2 = niedrig**, **3–4 = mittel**, **6–9 = hoch**

## Bedrohungen und Risiken

| Nr. | Bedrohung | Betroffenes Schutzziel | W | S | Risiko | Im Projekt? |
|---|---|---|---|---|---|---|
| R1 | **Fly-Hack** (fliegen ohne Erlaubnis) | Integrität | 3 | 3 | **9 hoch** | ✅ FlyCheck |
| R2 | **Speed-Hack** (zu schnelles Laufen) | Integrität | 3 | 2 | **6 hoch** | ✅ SpeedCheck |
| R3 | **Reach** (Angriff aus zu großer Entfernung) | Integrität | 2 | 3 | **6 hoch** | ✅ ReachCheck |
| R4 | **KillAura/Autoclicker** (automatisches Angreifen) | Integrität | 3 | 3 | **9 hoch** | ✅ KillAuraCheck |
| R5 | **NoFall** (kein Fallschaden) | Integrität | 2 | 2 | **4 mittel** | ✅ NoFallCheck |
| R6 | X-Ray/ESP (versteckte Erze/Spieler sehen) | Vertraulichkeit | 3 | 2 | 6 hoch | ❌ Grenze, siehe 05 |
| R7 | Missbrauch von Admin-Rechten | alle | 1 | 3 | 3 mittel | ⚙️ organisatorisch |
| R8 | Datenverlust der Welt (Defekt, Griefing) | Verfügbarkeit, Integrität | 2 | 3 | 6 hoch | ⚙️ Backups |
| R9 | Veraltete Server-Software / unsichere Plugins | alle | 2 | 3 | 6 hoch | ⚙️ Updates, nur vertrauenswürdige Quellen |
| R10 | DDoS-Angriff auf Port 25565 | Verfügbarkeit | 1 | 3 | 3 mittel | ❌ außerhalb des Projekts |
| R11 | Fehlalarme sperren ehrliche Spieler aus | Verfügbarkeit | 2 | 2 | 4 mittel | ✅ Grenzwerte, Toleranzen |
| R12 | Unzulässige Speicherung personenbezogener Daten | Vertraulichkeit (Recht) | 2 | 2 | 4 mittel | ✅ Datensparsame Logs |

W = Wahrscheinlichkeit, S = Schadenshöhe

## Risikomatrix

```
Schaden  3 |  R7 R10      | R3 R8 R9     | R1 R4
         2 |              | R5 R11 R12   | R2 R6
         1 |              |              |
           +--------------+--------------+--------------
                 1              2              3     Wahrscheinlichkeit
```

## Risikobehandlung

| Strategie | Bedeutung | Bei uns |
|---|---|---|
| **Vermeiden** | Risiko gar nicht erst eingehen | z. B. Server nur für Whitelist-Spieler öffnen |
| **Vermindern** | Wahrscheinlichkeit oder Schaden senken | **Anticheat** (R1–R5), Backups (R8), Updates (R9) |
| **Übertragen** | Risiko an Dritte abgeben | DDoS-Schutz beim Hoster (R10) |
| **Akzeptieren** | Restrisiko bewusst tragen | X-Ray (R6) und Cheats, die unter den Grenzwerten bleiben |

Die konkreten Maßnahmen stehen in [04-massnahmen.md](04-massnahmen.md).
