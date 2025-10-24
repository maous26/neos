# MAPPING DES FICHIERS - UI TV COMPLÈTE

## NOUVEAUX FICHIERS À CRÉER (10 fichiers)

### Activités Kotlin (5 fichiers)

#### 1. TvHomeActivity.kt
**Source:** `TvHomeActivity.kt`
**Destination:** `app/src/main/java/com/neostream/app/ui/home/TvHomeActivity.kt`
**Action:** CRÉER (nouveau dossier `home/`)
**Fonction:** Écran d'accueil avec grandes cartes catégories

#### 2. CategoryBrowseActivity.kt
**Source:** `CategoryBrowseActivity.kt`
**Destination:** `app/src/main/java/com/neostream/app/ui/browse/CategoryBrowseActivity.kt`
**Action:** CRÉER (nouveau dossier `browse/`)
**Fonction:** Navigation dans une catégorie (affiche les groupes)

#### 3. ChannelsGridActivity.kt
**Source:** `ChannelsGridActivity.kt`
**Destination:** `app/src/main/java/com/neostream/app/ui/browse/ChannelsGridActivity.kt`
**Action:** CRÉER (dans dossier `browse/`)
**Fonction:** Grille de chaînes d'un groupe spécifique

#### 4. CountriesActivity.kt
**Source:** `CountriesActivity.kt`
**Destination:** `app/src/main/java/com/neostream/app/ui/browse/CountriesActivity.kt`
**Action:** CRÉER (dans dossier `browse/`)
**Fonction:** Filtrage par pays (FR, AR, ES, IT...)

#### 5. QualityFilterActivity.kt
**Source:** `QualityFilterActivity.kt`
**Destination:** `app/src/main/java/com/neostream/app/ui/browse/QualityFilterActivity.kt`
**Action:** CRÉER (dans dossier `browse/`)
**Fonction:** Filtrage par qualité (4K, Full HD, HD, HEVC)

---

### Layouts XML (5 fichiers)

#### 6. activity_tv_home.xml
**Source:** `activity_tv_home.xml`
**Destination:** `app/src/main/res/layout/activity_tv_home.xml`
**Action:** CRÉER
**Contenu:** Grid de cartes colorées pour catégories

#### 7. activity_category_browse.xml
**Source:** `activity_category_browse.xml`
**Destination:** `app/src/main/res/layout/activity_category_browse.xml`
**Action:** CRÉER
**Contenu:** Liste/grid des groupes dans une catégorie

#### 8. activity_channels_grid.xml
**Source:** `activity_channels_grid.xml`
**Destination:** `app/src/main/res/layout/activity_channels_grid.xml`
**Action:** CRÉER
**Contenu:** Grid de chaînes avec métadonnées

#### 9. activity_countries.xml
**Source:** `activity_countries.xml`
**Destination:** `app/src/main/res/layout/activity_countries.xml`
**Action:** CRÉER
**Contenu:** Grid de pays avec drapeaux

#### 10. activity_quality_filter.xml
**Source:** `activity_quality_filter.xml`
**Destination:** `app/src/main/res/layout/activity_quality_filter.xml`
**Action:** CRÉER
**Contenu:** Liste des qualités disponibles

---

## FICHIER À REMPLACER (1 fichier)

#### 11. AndroidManifest.xml
**Source:** `AndroidManifest_TV.xml`
**Destination:** `app/src/main/AndroidManifest.xml`
**Action:** REMPLACER
**Changements:** 
- TvHomeActivity comme launcher (nouvel écran d'accueil)
- Déclaration des 4 nouvelles activités browse
- MainActivity reste accessible mais pas en launcher

---

## COMMANDES D'INSTALLATION

### Méthode 1: Script Automatique (RECOMMANDÉ)
```bash
chmod +x install_tv_ui.sh
./install_tv_ui.sh
```

### Méthode 2: Commandes Manuelles
```bash
# 1. Créer les dossiers
mkdir -p app/src/main/java/com/neostream/app/ui/home
mkdir -p app/src/main/java/com/neostream/app/ui/browse

# 2. Copier les activités Kotlin
cp TvHomeActivity.kt app/src/main/java/com/neostream/app/ui/home/
cp CategoryBrowseActivity.kt app/src/main/java/com/neostream/app/ui/browse/
cp ChannelsGridActivity.kt app/src/main/java/com/neostream/app/ui/browse/
cp CountriesActivity.kt app/src/main/java/com/neostream/app/ui/browse/
cp QualityFilterActivity.kt app/src/main/java/com/neostream/app/ui/browse/

# 3. Copier les layouts XML
cp activity_tv_home.xml app/src/main/res/layout/
cp activity_category_browse.xml app/src/main/res/layout/
cp activity_channels_grid.xml app/src/main/res/layout/
cp activity_countries.xml app/src/main/res/layout/
cp activity_quality_filter.xml app/src/main/res/layout/

# 4. Remplacer AndroidManifest
cp AndroidManifest_TV.xml app/src/main/AndroidManifest.xml

# 5. Rebuild
./gradlew clean assembleDebug installDebug
```

---

## STRUCTURE FINALE DES DOSSIERS

```
NEOSTREAM/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/neostream/app/
│           │   ├── MainActivity.kt (existant, non launcher)
│           │   ├── ui/
│           │   │   ├── home/
│           │   │   │   └── TvHomeActivity.kt ← NOUVEAU (LAUNCHER)
│           │   │   ├── browse/
│           │   │   │   ├── CategoryBrowseActivity.kt ← NOUVEAU
│           │   │   │   ├── ChannelsGridActivity.kt ← NOUVEAU
│           │   │   │   ├── CountriesActivity.kt ← NOUVEAU
│           │   │   │   └── QualityFilterActivity.kt ← NOUVEAU
│           │   │   ├── imports/
│           │   │   │   └── AddSourceActivity.kt (existant)
│           │   │   └── ...
│           │   └── ...
│           ├── res/
│           │   └── layout/
│           │       ├── activity_tv_home.xml ← NOUVEAU
│           │       ├── activity_category_browse.xml ← NOUVEAU
│           │       ├── activity_channels_grid.xml ← NOUVEAU
│           │       ├── activity_countries.xml ← NOUVEAU
│           │       ├── activity_quality_filter.xml ← NOUVEAU
│           │       └── ... (existants)
│           └── AndroidManifest.xml ← REMPLACÉ
└── ...
```

---

## VÉRIFICATION POST-INSTALLATION

### Vérifier que tous les fichiers existent:
```bash
# Kotlin files
ls -l app/src/main/java/com/neostream/app/ui/home/TvHomeActivity.kt
ls -l app/src/main/java/com/neostream/app/ui/browse/CategoryBrowseActivity.kt
ls -l app/src/main/java/com/neostream/app/ui/browse/ChannelsGridActivity.kt
ls -l app/src/main/java/com/neostream/app/ui/browse/CountriesActivity.kt
ls -l app/src/main/java/com/neostream/app/ui/browse/QualityFilterActivity.kt

# XML layouts
ls -l app/src/main/res/layout/activity_tv_home.xml
ls -l app/src/main/res/layout/activity_category_browse.xml
ls -l app/src/main/res/layout/activity_channels_grid.xml
ls -l app/src/main/res/layout/activity_countries.xml
ls -l app/src/main/res/layout/activity_quality_filter.xml

# Manifest
ls -l app/src/main/AndroidManifest.xml
```

### Vérifier que TvHomeActivity est le launcher:
```bash
grep -A5 "TvHomeActivity" app/src/main/AndroidManifest.xml | grep "LAUNCHER"
```

Devrait afficher:
```
<category android:name="android.intent.category.LEANBACK_LAUNCHER" />
<category android:name="android.intent.category.LAUNCHER" />
```

---

## RÉCAPITULATIF

| Type | Source | Destination | Action |
|------|--------|-------------|--------|
| Kotlin | TvHomeActivity.kt | ui/home/ | CRÉER |
| Kotlin | CategoryBrowseActivity.kt | ui/browse/ | CRÉER |
| Kotlin | ChannelsGridActivity.kt | ui/browse/ | CRÉER |
| Kotlin | CountriesActivity.kt | ui/browse/ | CRÉER |
| Kotlin | QualityFilterActivity.kt | ui/browse/ | CRÉER |
| XML | activity_tv_home.xml | res/layout/ | CRÉER |
| XML | activity_category_browse.xml | res/layout/ | CRÉER |
| XML | activity_channels_grid.xml | res/layout/ | CRÉER |
| XML | activity_countries.xml | res/layout/ | CRÉER |
| XML | activity_quality_filter.xml | res/layout/ | CRÉER |
| XML | AndroidManifest_TV.xml | AndroidManifest.xml | REMPLACER |

**Total:** 10 nouveaux fichiers + 1 remplacement = 11 fichiers

---

## EN CAS DE PROBLÈME

### Erreur "Cannot resolve symbol"
→ Les ViewBinding classes ne sont pas générées
→ Solution: `./gradlew clean build`

### Erreur "Activity not found"
→ L'activité n'est pas dans le manifest
→ Vérifier AndroidManifest.xml

### Erreur de package
→ Les fichiers ne sont pas dans les bons dossiers
→ Vérifier la structure ci-dessus

### App ne démarre pas
→ ViewBinding pas activé
→ Vérifier `buildFeatures { viewBinding true }` dans build.gradle
