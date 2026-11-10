# Git-Anleitung

## Einmalig einrichten
```bash
git config --global user.name "Dein Name"
git config --global user.email "deine@mail.de"
```

## Mitschüler als Collaborator hinzufügen
Repository auf GitHub → **Settings → Collaborators → Add people** → GitHub-Name eingeben → Einladung per Mail annehmen.

## Die wichtigsten Befehle
| Befehl | Bedeutung |
|--------|-----------|
| `git clone <url>` | Repository erstmals herunterladen |
| `git pull` | neueste Änderungen holen |
| `git branch` | Branches anzeigen |
| `git checkout -b feature/x` | neuen Branch erstellen und wechseln |
| `git checkout develop` | Branch wechseln |
| `git status` | was wurde geändert? |
| `git add <datei>` / `git add .` | Änderungen für den Commit vormerken |
| `git commit -m "feat: ..."` | Änderungen speichern |
| `git push -u origin feature/x` | Branch hochladen (beim 1. Mal mit `-u`) |
| `git log --oneline` | Verlauf anzeigen |

## Merge-Konflikte lösen
Ein Konflikt entsteht, wenn ihr dieselbe Stelle geändert habt.
1. `git checkout feature/x` und `git merge develop` (oder `git pull`).
2. Git meldet `CONFLICT`. Datei öffnen, Markierungen suchen:
   ```
   <<<<<<< HEAD
   eure Version
   =======
   Version der anderen
   >>>>>>> develop
   ```
3. Entscheiden, was bleibt, und die drei Markierungszeilen löschen.
4. `git add <datei>` und `git commit` (Merge abschließen), dann `git push`.

## Branch-Schutz für main (auf GitHub)
Settings → Branches → **Add branch ruleset** (oder Branch protection rule) für `main`:
- Require a pull request before merging
- Required approvals: 1
- Require status checks to pass → `build`
Dasselbe optional für `develop`.

## Projects Board
Reiter **Projects → New project → Board**. Spalten: To Do / In Progress / Done. Pro Check ein Issue anlegen und zum Board hinzufügen.
