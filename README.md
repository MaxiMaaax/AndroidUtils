# AndroidUtils

Application macOS standalone pour gérer des appareils Android. Utilitaire Compose Multiplatform qui permet de lancer scrcpy et de télécharger des APK depuis des appareils Android connectés.

## Fonctionnalités

- 🔌 **Détection automatique des appareils** connectés via ADB
- 📱 **Lancement de scrcpy** pour contrôler l'appareil à distance
- 📦 **Téléchargement d'APK** depuis l'appareil vers l'ordinateur
- 🚀 **Standalone** : ADB et scrcpy sont intégrés dans l'application

## Build et développement

### Prérequis

- Java 17+
- Gradle
- macOS (pour générer le DMG)

### Build local

```bash
# Télécharger les binaires nécessaires (adb et scrcpy)
chmod +x scripts/download_binaries.sh
./scripts/download_binaries.sh

# Lancer l'application
./gradlew run

# Build le DMG
./gradlew packageDmg
```

Le DMG sera généré dans `build/compose/binaries/main/dmg/`.

## Versioning automatique

L'application utilise un système de versioning automatique basé sur les conventions de commit (Conventional Commits). À chaque merge dans `main`, le workflow GitHub Actions analyse les commits depuis le dernier tag et incrémente la version appropriée.

### Comment ça fonctionne

Le workflow analyse tous les commits depuis le dernier tag (ou tous les commits s'il n'y a pas de tag) et détermine automatiquement quel chiffre incrémenter dans `vX.Y.Z` :

#### MAJOR (X++) - Changements incompatibles
Un commit est considéré comme un changement MAJOR si :
- Le message contient `BREAKING CHANGE:` dans le corps du commit
- Le type de commit contient `!` : `feat!: description` ou `feat(scope)!: description`

**Exemple :**
```
feat!: changer l'API de gestion des appareils
```
→ `v1.2.3` devient `v2.0.0`

#### MINOR (Y++) - Nouvelles fonctionnalités
Un commit est considéré comme un changement MINOR si :
- Le message commence par `feat:` ou `feat(scope):`

**Exemple :**
```
feat: ajouter filtrage des packages
feat(ui): améliorer l'interface de sélection
```
→ `v1.2.3` devient `v1.3.0`

#### PATCH (Z++) - Corrections de bugs
Par défaut, tous les autres commits sont considérés comme des PATCH :
- `fix: description`
- `chore: description`
- `docs: description`
- `refactor: description`
- etc.

**Exemple :**
```
fix: corriger bug de téléchargement APK
chore: mettre à jour les dépendances
docs: améliorer la documentation
```
→ `v1.2.3` devient `v1.2.4`

### Ordre de priorité

Si plusieurs types de commits sont présents entre deux tags, c'est le plus important qui est utilisé :
1. **MAJOR** (priorité la plus haute) - stoppe l'analyse et incrémente X
2. **MINOR** - incrémente Y si aucune MAJOR n'a été trouvée
3. **PATCH** - incrémente Z par défaut

### Exemples concrets

| Commits depuis le dernier tag | Version avant | Version après | Type |
|-------------------------------|---------------|---------------|------|
| `fix: corriger bug` | v1.0.0 | v1.0.1 | PATCH |
| `feat: nouvelle fonctionnalité` | v1.0.1 | v1.1.0 | MINOR |
| `feat: nouvelle fonctionnalité`<br>`fix: bug` | v1.0.1 | v1.1.0 | MINOR |
| `feat!: changement incompatible` | v1.1.0 | v2.0.0 | MAJOR |
| `fix: bug`<br>`feat: nouvelle fonctionnalité` | v1.0.0 | v1.1.0 | MINOR |
| `feat: nouvelle fonctionnalité`<br>`feat!: changement incompatible` | v1.0.0 | v2.0.0 | MAJOR |

### Conventions de commit recommandées

Pour que le système fonctionne correctement, utilisez ces formats :

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Types principaux :**
- `feat`: Nouvelle fonctionnalité
- `fix`: Correction de bug
- `docs`: Documentation uniquement
- `style`: Changements de formatage
- `refactor`: Refactoring du code
- `perf`: Amélioration des performances
- `test`: Ajout ou modification de tests
- `chore`: Tâches de maintenance

**Exemples :**
```
feat(ui): ajouter sélection de dossier de sortie
fix: corriger crash lors du pull APK
feat!: refactoriser l'API ADB (BREAKING CHANGE: nouvelle méthode)
chore: mettre à jour Gradle vers 8.0
docs: ajouter section versioning dans README
```

## GitHub Actions

Le workflow `.github/workflows/release.yml` :

1. **Se déclenche** sur chaque push vers `main`
2. **Analyse les commits** depuis le dernier tag pour déterminer le type de bump
3. **Met à jour la version** dans `build.gradle.kts` (champs `version` et `packageVersion`)
4. **Crée un commit** `chore(release): vX.Y.Z`
5. **Crée un tag** `vX.Y.Z`
6. **Télécharge les binaires** (adb et scrcpy) si nécessaire
7. **Build le DMG** pour macOS
8. **Upload l'artefact** DMG en pièce jointe du workflow

### Protection contre les boucles

Le workflow ignore automatiquement les commits qui commencent par `chore(release):` pour éviter les boucles infinies lors du commit de release automatique.

### Accès aux releases

Une fois le workflow terminé, vous pouvez :
- Voir le tag créé dans l'onglet "Releases" de GitHub
- Télécharger le DMG depuis les artefacts du workflow GitHub Actions

## Structure du projet

```
AndroidUtils/
├── .github/workflows/
│   └── release.yml          # Workflow de release automatique
├── scripts/
│   └── download_binaries.sh  # Script pour télécharger adb et scrcpy
├── src/main/
│   ├── kotlin/
│   │   ├── Main.kt           # Application Compose Desktop
│   │   └── com/maximaaax/android/utils/
│   │       ├── AdbManager.kt        # Gestion ADB (devices, packages, pull)
│   │       ├── ScrcpyLauncher.kt    # Lancement de scrcpy
│   │       └── ResourceExtractor.kt # Extraction des binaires depuis resources
│   └── resources/
│       └── bin/darwin-arm64/  # Binaires adb et scrcpy pour macOS ARM64
└── build.gradle.kts          # Configuration Gradle et Compose Desktop
```

## Notes techniques

- Les binaires `adb` et `scrcpy` sont extraits depuis les resources vers `~/.androidutils/bin/` au premier lancement
- L'application nécessite macOS pour fonctionner (binaires compilés pour darwin-arm64)
- Le packaging génère un `.app` macOS et un `.dmg` pour distribution

