# Erstes Feature: kompletter Ablauf

Beispiel: `feature/speed-check` (Marcel) – Review durch Niklas.

1. **Issue**: GitHub → Issues → New → „Feature Request“: „feat: Speed-Check“. Dir zuweisen, im Board auf *In Progress* ziehen (z. B. Issue #3).
2. **Branch**:
   ```bash
   git checkout develop
   git pull
   git checkout -b feature/speed-check
   ```
3. **Commits** (klein und oft):
   ```bash
   git add .
   git commit -m "feat: add speed check skeleton"
   ```
4. **Push**: `git push -u origin feature/speed-check`
5. **Pull Request**: GitHub zeigt „Compare & pull request“. Ziel `develop`, Text `Closes #3`. Der grüne Haken (Build) erscheint nach ein, zwei Minuten.
6. **Review** (Niklas): *Files changed* ansehen, Kommentare an Zeilen schreiben, dann *Review changes → Approve*.
7. **Merge**: *Merge pull request* (Marcel). Issue schließt automatisch, Board → *Done*.
8. **Aufräumen**:
   ```bash
   git checkout develop
   git pull
   git branch -d feature/speed-check
   ```
