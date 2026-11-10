# Testserver einrichten (Windows)

Der Testserver läuft auf einem Windows-PC zu Hause und bleibt online, solange der PC an ist.
Alles Nötige liegt im Ordner [`testserver/`](../testserver):

| Datei | Zweck |
|---|---|
| `start.bat` | Startet den Server – lädt beim ersten Mal Paper, legt die Konfiguration an, kopiert das Plugin |
| `paper-download.ps1` | Lädt Paper über die offizielle PaperMC-API und prüft die SHA-256-Prüfsumme |
| `server.properties.vorlage` | Gehärtete Server-Konfiguration (siehe unten) |
| `run/` | Entsteht beim Start: Welt, Logs, Plugins. **Wird nicht ins Git hochgeladen.** |

## 1. Voraussetzungen

- **Java 25.** `start.bat` sucht automatisch ein installiertes Temurin 25 oder das JDK, das Gradle beim Bauen geladen hat. Wenn nichts gefunden wird:
  ```bash
  winget install EclipseAdoptium.Temurin.25.JDK
  ```
- **Plugin bauen** (im Projektordner):
  ```bash
  gradlew.bat build
  ```

## 2. Server starten

Doppelklick auf `testserver\start.bat`. Beim **ersten** Start:
1. Paper wird heruntergeladen (ca. 55 MB).
2. `run\server.properties` wird aus der Vorlage erstellt.
3. Das Skript fragt, ob ihr die [Minecraft-EULA](https://aka.ms/MinecraftEULA) akzeptiert – lest sie und bestätigt mit **J**.
4. Der Server erzeugt die Welt. Fertig, wenn in der Konsole `Done (...)! For help, type "help"` steht.

Nach jedem neuen Build einfach `start.bat` neu starten – das Plugin wird automatisch kopiert.

**Stoppen:** In der Server-Konsole `stop` eingeben, dann beim Neustart-Hinweis **N** drücken.
Ohne Eingabe startet der Server nach 15 Sekunden neu (praktisch nach einem Absturz).

## 3. Spieler freischalten und Admin-Rechte

Die Whitelist ist eingeschaltet, d. h. nur eingetragene Spieler kommen rein. In der Server-Konsole:
```
whitelist add <Minecraft-Name>
op <Minecraft-Name>
```
`op` nur für euch beide (Admins sehen später die Anticheat-Warnungen).
Für die Demo auch den Account mit dem Cheat-Client auf die Whitelist setzen – aber **nicht** op geben.

## 4. Verbinden im LAN (zu Hause / Demo)

- Auf demselben PC: Serveradresse `localhost`
- Von einem anderen Gerät im selben Netz: die LAN-IP des Server-PCs, z. B. `192.168.2.73`
  (herausfinden mit `ipconfig` → „IPv4-Adresse“ beim WLAN- bzw. Ethernet-Adapter)

Windows fragt beim ersten Start, ob Java im Netzwerk erlaubt sein soll → nur **Private Netzwerke** erlauben.
Alternativ eine gezielte Firewall-Regel (PowerShell **als Administrator**):
```powershell
New-NetFirewallRule -DisplayName "Minecraft Testserver" -Direction Inbound -Protocol TCP -LocalPort 25565 -Action Allow -Profile Private
```

## 5. Über das Internet erreichbar machen (optional)

### Variante A: Portfreigabe im Router
1. Router-Oberfläche öffnen (meist die Gateway-Adresse, z. B. `http://192.168.2.1`).
2. Portfreigabe einrichten: **TCP 25565** → LAN-IP des Server-PCs.
3. Dem Server-PC im Router eine **feste LAN-IP** geben, sonst stimmt die Freigabe nach einem Neustart evtl. nicht mehr.
4. Eigene öffentliche IP herausfinden (z. B. im Router unter „Internet“) und Freunden geben.

**Prüfen, ob das bei euch klappt (CGNAT/DS-Lite):** Vergleicht die IPv4-Adresse, die der Router als
„Internet-Adresse“ anzeigt, mit der auf z. B. `https://ifconfig.me`. Sind sie **verschieden** oder
beginnt die Router-Adresse mit `100.64.`–`100.127.`, habt ihr keine eigene öffentliche IPv4 –
dann geht Variante A nicht, nehmt Variante B.

**Wechselnde IP:** Viele Anschlüsse bekommen regelmäßig eine neue IP. Abhilfe: kostenloser DynDNS-Dienst
(z. B. DuckDNS oder der DynDNS-Dienst eures Routers).

### Variante B: Tunnel ohne Router-Einstellungen
Dienste wie **playit.gg** (kostenlose Stufe) leiten Verbindungen über ihre Server zu euch weiter.
Funktioniert auch bei CGNAT. Das Programm läuft neben dem Server auf dem PC.

### Sicherheit beim Betrieb im Internet
Ein offener Port macht den PC aus dem Internet erreichbar. Deshalb:
- **nur** TCP 25565 freigeben, sonst nichts
- Whitelist an lassen, `online-mode=true` lassen
- Windows und Paper aktuell halten (`run\paper.jar` löschen → `start.bat` lädt die neueste Version)
- Die Risiken stehen in [lf11b/03-risikoanalyse.md](lf11b/03-risikoanalyse.md) (R9, R10)

## 6. Dauerbetrieb („bleibt online, während wir in der Schule sind“)

- **Energiesparen:** *Einstellungen → System → Energie* → „Bildschirm aus“ ist okay, aber **„Energiesparmodus/Standby: Nie“** (bei Netzbetrieb).
- **Windows-Updates:** Unter *Windows Update → Erweiterte Optionen → Nutzungszeit* so einstellen, dass Windows nicht vormittags neu startet.
- **Abstürze:** `start.bat` startet den Server nach 15 Sekunden automatisch neu.
- **Backups:** Ordner `testserver\run\world*` ab und zu kopieren (oder Server stoppen und den ganzen `run`-Ordner sichern).

## 7. Warum die Vorlage so eingestellt ist

| Einstellung | Wert | Warum | Soll-Kriterium |
|---|---|---|---|
| `online-mode` | `true` | Nur echte Microsoft-Accounts – keine gefälschten Namen | S8 |
| `enforce-secure-profile` | `true` | Chat-Nachrichten sind signiert | S8 |
| `white-list` / `enforce-whitelist` | `true` | Server läuft dauerhaft öffentlich – nur bekannte Spieler | – |
| `enable-rcon` / `enable-query` | `false` | Keine Fernwartung, keine zusätzlichen offenen Ports | S9 |
| `log-ips` | `false` | Keine IP-Adressen im Server-Log (DSGVO, Datensparsamkeit) | S6 |
| `allow-flight` | `true` | Minecraft hat einen eigenen einfachen Flug-Kick. Den schalten wir ab, damit **unser** Fly-Check die Erkennung übernimmt und in der Demo sichtbar wird. | S1 |
| `max-players` | `10` | Kleiner Testserver | – |

> Hinweis zu `allow-flight=true`: Das ist eine bewusste Entscheidung für die Demo. Ohne unser
> Anticheat wäre ein Server damit schlechter geschützt als im Standard – genau das zeigt der IST-Zustand.
