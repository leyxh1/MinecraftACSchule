# Sicherheitsprüfung und Bewertung

> Wird nach Phase 3 mit echten Testergebnissen ausgefüllt.

## Testplan

### A) Erkennt das Anticheat Cheats? (Wirksamkeit)

| Nr. | Test (mit Cheat-Client auf eigenem Server) | Erwartet | Ergebnis | ✔/✘ |
|---|---|---|---|---|
| T1 | Fly-Modul einschalten, 5 s in der Luft bleiben | Warnung „Fly“, VL steigt, ab Grenzwert Kick | | |
| T2 | Speed-Modul einschalten, geradeaus laufen | Warnung „Speed“ | | |
| T3 | Reach-Modul, Gegner aus ca. 5 Blöcken schlagen | Warnung „Reach“ | | |
| T4 | KillAura einschalten, mehrere Mobs/Spieler in der Nähe | Warnung „KillAura“ (Klickrate/Blickwinkel) | | |
| T5 | NoFall einschalten, aus 20 Blöcken springen | Warnung „NoFall“ | | |
| T6 | `/ac status <Spieler>` | Zeigt aktuelle VL pro Check | | |
| T7 | Logdatei prüfen | Einträge mit Zeit, Spieler, Check, VL | | |

### B) Fehlalarme bei normalem Spielen? (Akzeptanz)

| Nr. | Test (normaler Client) | Erwartet | Ergebnis | ✔/✘ |
|---|---|---|---|---|
| F1 | Sprinten + Springen auf Eis | keine Warnung | | |
| F2 | Speed-II-Trank, sprinten | keine Warnung | | |
| F3 | Elytra-Flug | keine Warnung | | |
| F4 | Von Mob/Spieler weggeschlagen werden (Knockback) | keine Warnung | | |
| F5 | Treppen und Stufen hochlaufen | keine Warnung | | |
| F6 | In Wasser / Spinnennetz / Heuballen fallen | keine NoFall-Warnung | | |
| F7 | Normal kämpfen, schnell klicken (~10 CPS) | keine Warnung | | |
| F8 | Künstlicher Lag (z. B. WLAN statt LAN) | höchstens einzelne Warnungen, kein Kick | | |

## Soll-Ist-Vergleich

Kriterien aus [00-auswahl-und-sollzustand.md](00-auswahl-und-sollzustand.md#soll-zustand-sicherheitskriterien):

| Nr. | Kriterium | Nachweis (Test/Screenshot) | erfüllt? |
|---|---|---|---|
| S1 | Cheats werden erkannt (mind. 4 von 5) | T1–T5 | |
| S2 | Auto-Kick ab Grenzwert | T1–T5 | |
| S3 | Kein Kick bei normalem Spiel | F1–F8 | |
| S4 | Admin-Warnungen | T1–T5 | |
| S5 | Protokollierung | T7 | |
| S6 | Keine IP-Adressen im Anticheat-Log | T7 | |
| S7 | Grenzwerte per `config.yml` | Konfig ändern, Verhalten testen | |
| S8 | `online-mode=true` | `server.properties` | |
| S9 | Nur Port 25565 offen, RCON aus | Firewall-Screenshot, `server.properties` | |
| S10 | Vier-Augen-Prinzip | GitHub-Ruleset, gemergte PRs mit Approval | |

## Bewertung des Sicherheitsniveaus

| Risiko | vorher | nachher | Begründung |
|---|---|---|---|
| R1 Fly | hoch | | |
| R2 Speed | hoch | | |
| R3 Reach | hoch | | |
| R4 KillAura | hoch | | |
| R5 NoFall | mittel | | |
| R11 Fehlalarme | mittel | | |

## Grenzen serverseitiger Checks (Restrisiko)

- **Unterhalb der Grenzwerte** cheaten bleibt unerkannt (z. B. Reach 3,2 statt 3,0 Blöcke).
- **Lag** zwingt uns zu Toleranzen – je toleranter, desto mehr Spielraum für Cheater.
- **Informations-Cheats** (X-Ray, ESP, Minimap) verändern nichts, was der Server sieht – nicht erkennbar mit unseren Checks.
- Cheat-Clients haben oft **Bypass-Modi**, die genau auf bekannte Anticheats abgestimmt sind.
- Ein Anticheat ersetzt keine Moderation: Am Ende entscheidet ein Mensch.

## Wirtschaftlichkeit

| Lösung | Kosten | Aufwand | Erkennungsqualität | Bewertung |
|---|---|---|---|---|
| Kein Anticheat | 0 € | 0 h | keine | Spieler wandern ab |
| **Eigenes Plugin (unser Projekt)** | 0 € Lizenz | ca. __ h Entwicklung + Pflege | Grundschutz gegen offensichtliche Cheats | gut zum Lernen, für kleine Server ausreichend |
| Fertiges Open-Source-Anticheat (z. B. Grim) | 0 € | gering (installieren, konfigurieren) | hoch, von Community gepflegt | für Produktivbetrieb empfehlenswert |
| Kommerzielles Anticheat | einmalig ca. 10–30 € | gering | hoch, mit Support | für größere Server |
| Nur manuelle Moderation | Personalzeit | hoch, dauerhaft | abhängig vom Personal | skaliert nicht |

Preise und Aufwände vor der Abgabe noch einmal prüfen und mit eurer tatsächlichen
Stundenzahl aus den Commits/Issues ergänzen.

**Fazit:** _(nach den Tests ausfüllen)_
