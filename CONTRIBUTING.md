# Mitarbeit / Team-Regeln

## Branches
- `main` – nur fertiger, funktionierender Stand (geschützt, nur per PR).
- `develop` – Zwischenstand, hier landen Features zuerst.
- `feature/<name>` – z. B. `feature/speed-check`; `fix/<name>` für Fehler.

## Ablauf für jede Änderung
1. Issue anlegen oder ein bestehendes nehmen und sich zuweisen.
2. Von `develop` einen Branch abzweigen.
3. Kleine, sinnvolle Commits machen.
4. Branch pushen und Pull Request nach `develop` öffnen (`Closes #Nr`).
5. Die andere Person reviewt und approved. **Niemand mergt seinen eigenen PR ohne Approval.**
6. Merge, danach Branch löschen.
7. Für Releases: PR `develop` → `main`, dann Tag setzen.

## Commit-Nachrichten (Conventional Commits)
`typ: kurze Beschreibung` – Typen: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`.

Beispiele: `feat: add reach check`, `fix: false positive on ice`, `docs: describe fly check`.

## Code-Regeln
- Einfacher Code, deutsche Kommentare – wir müssen jede Zeile erklären können.
- Jeder Check in einer eigenen Klasse.
- Grenzwerte gehören in die `config.yml`, nicht in den Code.
- Vor dem PR: `./gradlew build` muss grün sein.

## Versionierung
[Semantic Versioning](https://semver.org/lang/de/): `MAJOR.MINOR.PATCH`. Änderungen in `CHANGELOG.md` festhalten.
