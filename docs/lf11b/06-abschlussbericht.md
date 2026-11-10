# Abschlussbericht an das Lehrerteam

> Vorlage – nach Abschluss der Tests ausfüllen.

**Lernfeld:** LF11 FISI – Szenario X
**Projekt:** Absicherung eines Minecraft-Servers gegen Cheat-Clients
**Team:** Marcel (@leyxh1), Niklas (@salkinhopsongit)
**Datum:** __.__.____
**Version des Anticheats:** v_._._
**Repository:** https://github.com/leyxh1/MinecraftACSchule

## 1. Gewähltes System und Ausgangslage
Kurzbeschreibung des Systems und des IST-Zustands ([00](00-auswahl-und-sollzustand.md), [02](02-schutzbedarf.md#ist-zustand)).

## 2. Schutzbedarf und Risiken
- Schutzbedarf: Integrität des Spielzustands **hoch** ([02](02-schutzbedarf.md))
- Größte Risiken: Fly, KillAura, Speed, Reach ([03](03-risikoanalyse.md))

## 3. Soll-Zustand
Vereinbarte Sicherheitskriterien S1–S10 und Datum der Abstimmung.

## 4. Umgesetzte Maßnahmen
- Anticheat-Plugin mit 5 Checks, Violation-System, Admin-Warnungen, Logdatei, Auto-Kick
- Härtung des Testservers (online-mode, Firewall, RCON aus, Backups)
- Sicherheitsleitlinie und rechtliche Umsetzung (DSGVO) ([04](04-massnahmen.md))

## 5. Prüfung und Soll-Ist-Vergleich
Ergebnisse aus [05](05-pruefung-bewertung.md): erkannte Cheats _ von 5, Fehlalarme _, Kriterien erfüllt _ von 10.

## 6. Bewertung
- Erreichtes Sicherheitsniveau
- Wirtschaftlichkeit (Aufwand in Stunden, Vergleich mit fertigen Lösungen)
- Restrisiken und Grenzen serverseitiger Checks

## 7. Fazit und Ausblick
Was haben wir gelernt? Was würden wir als Nächstes verbessern?

## 8. Arbeitsnachweis
Verteilung der Issues, Commits und Pull Requests (Screenshot vom Projects Board und von *Insights → Contributors*).
