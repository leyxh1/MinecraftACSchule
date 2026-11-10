# Die Checks im Detail

Alle Checks laufen **auf dem Server**. Sie verlassen sich nicht darauf, was der Client behauptet
(„never trust the client“), sondern rechnen selbst nach: Wo sind feste Blöcke? Wie stark wirkt
die Schwerkraft? Wie weit ist das Ziel wirklich weg?

| Check | Klasse | Erkennt | Wichtigste Werte in `config.yml` |
|---|---|---|---|
| Fly | [`FlyCheck`](../src/main/java/dev/schule/anticheat/checks/FlyCheck.java) | Fliegen, Schweben | `tolerance`, `buffer`, `hover-seconds` |
| Speed | [`SpeedCheck`](../src/main/java/dev/schule/anticheat/checks/SpeedCheck.java) | zu schnelles Laufen | `tolerance`, `air-bonus`, `ice-multiplier` |
| NoFall | [`NoFallCheck`](../src/main/java/dev/schule/anticheat/checks/NoFallCheck.java) | kein Fallschaden | `extra-distance`, `check-delay-ticks` |
| Reach | [`ReachCheck`](../src/main/java/dev/schule/anticheat/checks/ReachCheck.java) | Schlagen aus zu großer Entfernung | `tolerance`, `ping-factor` |
| KillAura | [`KillAuraCheck`](../src/main/java/dev/schule/anticheat/checks/KillAuraCheck.java) | automatisches Angreifen | `max-angle`, `snap-angle`, `max-targets` |
| AutoClicker | [`AutoClickerCheck`](../src/main/java/dev/schule/anticheat/checks/AutoClickerCheck.java) | zu viele/zu gleichmäßige Klicks | `max-cps`, `min-deviation-ms` |

## Gemeinsame Bausteine

- **Violation-Level (VL):** Jeder Verstoß gibt Punkte. Pro Sekunde sinken sie um `decay-per-second`.
  Erreicht das VL `kick-vl`, wird der Spieler gekickt. Ein einzelner Fehlalarm führt so nie zum Kick.
- **Puffer (`buffer`):** Viele Checks melden erst, wenn etwas **mehrmals hintereinander** auffällt.
  Normale Bewegungen bauen den Puffer wieder ab.
- **Meldungs-Pause (`flag-cooldown-ticks`):** verhindert, dass der Chat vollgespammt wird.
- **Allgemeine Ausnahmen** (in `Check.isExempt`): Kreativ/Zuschauer, Fliegen erlaubt, Fahrzeug,
  tot, 2 Sekunden nach dem Join, 1 Sekunde nach einem Teleport.

## Fly

**Idee:** In der Luft gilt immer dieselbe Formel: `neue Höhenänderung = (alte − 0,08) · 0,98`.
Wer deutlich davon abweicht (z. B. gar nicht fällt oder nach oben steigt), fliegt.
Zusätzlich zählt der Check, wie lange jemand **bewegungslos in der Luft** steht (Schweben).

**Keine Fehlalarme bei:** Elytra, Dreizack, Schwebe-/Sanfter-Fall-Effekt, Wasser, Lava, Leitern,
Ranken, Gerüst, Spinnennetz, Pulverschnee, Honig, Slime, Betten, Knockback, Kopf an der Decke,
Absprung und Landung (erst ab dem 2. Tick in der Luft wird gerechnet).

## Speed

**Idee:** Der Server kennt das Bewegungs-Attribut des Spielers (Gehen 0,10 / Sprinten 0,13 /
mit Speed-Trank mehr). Daraus wird die erlaubte Strecke pro Tick berechnet (`· 2,16`).
Danach gibt es Zuschläge:

| Situation | Zuschlag |
|---|---|
| Sprung / gerade gelandet | `+ air-bonus` (0,33) |
| auf Eis (bis 2 s danach) | `· ice-multiplier` (2,5) |
| Decke über dem Kopf | `+ head-bonus` (0,30) |
| Knockback (1,5 s) | `+ Stärke des Rückstoßes` |
| immer (Lag) | `· tolerance` (1,10) |

**Keine Fehlalarme bei:** Treppen und Stufen (Höhe zählt nicht mit), Sprinten, Speed-Effekt,
Eis, Knockback, Elytra, Wasser.

## NoFall

**Idee:** Fallschaden rechnet der Server erst aus, wenn der Client „ich bin gelandet“ meldet.
NoFall-Cheats fälschen genau diese Meldung. Wir messen die Fallhöhe deshalb **selbst**
(höchster Punkt − Landepunkt) und prüfen ein paar Ticks nach der Landung, ob Fallschaden
angekommen ist. Wenn nicht: Verstoß.

**Keine Fehlalarme bei:** Wasser, Heuballen, Slime, Betten, Spinnennetz, Pulverschnee, Sanfter
Fall, Elytra, Sprungkraft-Effekt (erhöht die sichere Fallhöhe), gerade erst Schaden bekommen.

## Reach

**Idee:** Abstand von den **Augen** des Angreifers bis zum **nächsten Punkt der Hitbox** des
Ziels. Erlaubt sind die normale Reichweite (Attribut, 3 Blöcke) + `tolerance` +
Ausgleich für Ping (`ping-factor` pro 50 ms, höchstens bis `max-ping`).

## KillAura

Drei Spuren einer KillAura:
1. **Blickfeld:** Das Ziel liegt mehr als `max-angle` Grad neben dem Fadenkreuz (z. B. hinter dem Spieler).
2. **Snap:** Der Kopf dreht sich in **einem** Tick um mehr als `snap-angle` Grad – direkt vor dem Schlag.
3. **Multi:** `max-targets` verschiedene Ziele innerhalb von `target-window-ticks`.

Ein einzelner schneller Schwenk ist normal, deshalb meldet der Check erst nach `buffer` Auffälligkeiten.

## AutoClicker

1. **Klickrate:** mehr als `max-cps` Klicks pro Sekunde.
2. **Gleichmäßigkeit:** Standardabweichung der letzten 20 Klick-Abstände unter `min-deviation-ms`.
   Menschen schwanken immer, Programme nicht.

Beim **Abbauen** von Blöcken schwingt der Arm dauernd – das wird ignoriert.

## Grenzwerte sinnvoll einstellen

1. **Erst normal spielen, dann cheaten.** Mit eingeschaltetem Anticheat ein paar Minuten ganz
   normal spielen (Sprinten, Springen, Eis, Treppen, Kämpfen, Elytra). Danach `violations.log`
   ansehen. Jeder Eintrag dort ist ein **Fehlalarm** → den betroffenen Wert lockerer stellen.
2. **Dann mit Cheat testen.** Modul im Cheat-Client einschalten und schauen, ob eine Meldung kommt.
   Kommt keine → Wert strenger stellen (kleinere Toleranz, kleinerer Puffer).
3. **Immer nur einen Wert ändern**, dann `/ac reload` und erneut testen.
4. **Lieber zu locker als zu streng.** Ein Fehlalarm ärgert ehrliche Spieler (Soll-Kriterium S3).
   Ein Cheater, der nur ganz knapp über dem Normalwert liegt, hat kaum einen Vorteil.

| Problem | Lösung |
|---|---|
| Fehlalarme Speed beim Sprint-Springen | `air-bonus` erhöhen (z. B. 0,40) |
| Fehlalarme Speed auf Eis | `ice-multiplier` erhöhen |
| Fehlalarme Reach bei hohem Ping | `ping-factor` oder `tolerance` erhöhen |
| Fly wird nicht erkannt | `buffer` verkleinern (z. B. 3) |
| zu schneller Kick in der Demo | `kick-vl` erhöhen oder `kick.enabled: false` |
| zu viele Meldungen im Chat | `flag-cooldown-ticks` erhöhen |

## Testen mit dem Cheat-Client (Kurzfassung)

Server starten, mit dem Cheat-Client joinen (Account auf der Whitelist), dann nacheinander:

| Modul (Meteor/Wurst) | Was tun | Erwartete Meldung |
|---|---|---|
| Flight / Fly | einschalten, abheben, in der Luft stehen bleiben | `Fly … Schwerkraft` bzw. `schwebt … in der Luft` |
| Speed | einschalten, geradeaus laufen (Wert im Client ggf. hochstellen) | `Speed … Blöcke/Tick` |
| NoFall | 10+ Blöcke hochbauen und runterspringen | `NoFall … ohne Schaden` |
| Reach | Reichweite auf 5–6 stellen, Mob aus der Entfernung schlagen | `Reach … Blöcke` |
| KillAura | bei mehreren Mobs einschalten | `KillAura …` |
| AutoClicker | einschalten (hohe CPS), in die Luft klicken | `AutoClicker …` |

Jederzeit: `/ac status <Spieler>` zeigt die aktuellen VL. Alle Meldungen stehen auch in
`testserver/run/plugins/SchoolAntiCheat/violations.log`.
