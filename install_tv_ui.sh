#!/bin/bash

# NEOSTREAM - Installation automatique UI TV Complète
# Ce script installe toute la nouvelle interface TV avec catégories

set -e  # Stop on error

echo "=================================================="
echo "NEOSTREAM - Installation UI TV Complète"
echo "=================================================="
echo ""

# Vérifier qu'on est à la racine du projet
if [ ! -f "settings.gradle" ]; then
    echo "❌ ERREUR: Vous devez lancer ce script depuis la racine du projet NEOSTREAM"
    echo "   (là où se trouve le fichier settings.gradle)"
    exit 1
fi

echo "✓ Racine du projet détectée"
echo ""

# Créer les nouveaux dossiers
echo "1. Création de la structure de dossiers..."
mkdir -p app/src/main/java/com/neostream/app/ui/home
mkdir -p app/src/main/java/com/neostream/app/ui/browse
echo "   ✓ app/src/main/java/com/neostream/app/ui/home"
echo "   ✓ app/src/main/java/com/neostream/app/ui/browse"
echo ""

# Vérifier que les fichiers source existent
OUTPUTS_DIR="outputs"
if [ ! -d "$OUTPUTS_DIR" ]; then
    echo "❌ ERREUR: Le dossier '$OUTPUTS_DIR' n'existe pas"
    echo "   Placez ce script dans le même dossier que les fichiers téléchargés"
    exit 1
fi

echo "2. Copie des nouveaux fichiers Kotlin..."

# TvHomeActivity
if [ -f "$OUTPUTS_DIR/TvHomeActivity.kt" ]; then
    cp "$OUTPUTS_DIR/TvHomeActivity.kt" app/src/main/java/com/neostream/app/ui/home/TvHomeActivity.kt
    echo "   ✓ TvHomeActivity.kt copié"
else
    echo "   ❌ TvHomeActivity.kt introuvable"
    exit 1
fi

# CategoryBrowseActivity
if [ -f "$OUTPUTS_DIR/CategoryBrowseActivity.kt" ]; then
    cp "$OUTPUTS_DIR/CategoryBrowseActivity.kt" app/src/main/java/com/neostream/app/ui/browse/CategoryBrowseActivity.kt
    echo "   ✓ CategoryBrowseActivity.kt copié"
else
    echo "   ❌ CategoryBrowseActivity.kt introuvable"
    exit 1
fi

# ChannelsGridActivity
if [ -f "$OUTPUTS_DIR/ChannelsGridActivity.kt" ]; then
    cp "$OUTPUTS_DIR/ChannelsGridActivity.kt" app/src/main/java/com/neostream/app/ui/browse/ChannelsGridActivity.kt
    echo "   ✓ ChannelsGridActivity.kt copié"
else
    echo "   ❌ ChannelsGridActivity.kt introuvable"
    exit 1
fi

# CountriesActivity
if [ -f "$OUTPUTS_DIR/CountriesActivity.kt" ]; then
    cp "$OUTPUTS_DIR/CountriesActivity.kt" app/src/main/java/com/neostream/app/ui/browse/CountriesActivity.kt
    echo "   ✓ CountriesActivity.kt copié"
else
    echo "   ❌ CountriesActivity.kt introuvable"
    exit 1
fi

# QualityFilterActivity
if [ -f "$OUTPUTS_DIR/QualityFilterActivity.kt" ]; then
    cp "$OUTPUTS_DIR/QualityFilterActivity.kt" app/src/main/java/com/neostream/app/ui/browse/QualityFilterActivity.kt
    echo "   ✓ QualityFilterActivity.kt copié"
else
    echo "   ❌ QualityFilterActivity.kt introuvable"
    exit 1
fi

echo ""
echo "3. Copie des nouveaux layouts XML..."

# activity_tv_home.xml
if [ -f "$OUTPUTS_DIR/activity_tv_home.xml" ]; then
    cp "$OUTPUTS_DIR/activity_tv_home.xml" app/src/main/res/layout/activity_tv_home.xml
    echo "   ✓ activity_tv_home.xml copié"
else
    echo "   ❌ activity_tv_home.xml introuvable"
    exit 1
fi

# activity_category_browse.xml
if [ -f "$OUTPUTS_DIR/activity_category_browse.xml" ]; then
    cp "$OUTPUTS_DIR/activity_category_browse.xml" app/src/main/res/layout/activity_category_browse.xml
    echo "   ✓ activity_category_browse.xml copié"
else
    echo "   ❌ activity_category_browse.xml introuvable"
    exit 1
fi

# activity_channels_grid.xml
if [ -f "$OUTPUTS_DIR/activity_channels_grid.xml" ]; then
    cp "$OUTPUTS_DIR/activity_channels_grid.xml" app/src/main/res/layout/activity_channels_grid.xml
    echo "   ✓ activity_channels_grid.xml copié"
else
    echo "   ❌ activity_channels_grid.xml introuvable"
    exit 1
fi

# activity_countries.xml
if [ -f "$OUTPUTS_DIR/activity_countries.xml" ]; then
    cp "$OUTPUTS_DIR/activity_countries.xml" app/src/main/res/layout/activity_countries.xml
    echo "   ✓ activity_countries.xml copié"
else
    echo "   ❌ activity_countries.xml introuvable"
    exit 1
fi

# activity_quality_filter.xml
if [ -f "$OUTPUTS_DIR/activity_quality_filter.xml" ]; then
    cp "$OUTPUTS_DIR/activity_quality_filter.xml" app/src/main/res/layout/activity_quality_filter.xml
    echo "   ✓ activity_quality_filter.xml copié"
else
    echo "   ❌ activity_quality_filter.xml introuvable"
    exit 1
fi

echo ""
echo "4. Remplacement du AndroidManifest.xml..."

# AndroidManifest.xml
if [ -f "$OUTPUTS_DIR/AndroidManifest_TV.xml" ]; then
    cp "$OUTPUTS_DIR/AndroidManifest_TV.xml" app/src/main/AndroidManifest.xml
    echo "   ✓ AndroidManifest.xml remplacé"
else
    echo "   ❌ AndroidManifest_TV.xml introuvable"
    exit 1
fi

echo ""
echo "=================================================="
echo "✅ TOUS LES FICHIERS ONT ÉTÉ COPIÉS AVEC SUCCÈS!"
echo "=================================================="
echo ""
echo "Structure installée:"
echo "  📺 TvHomeActivity (Écran d'accueil)"
echo "  📂 CategoryBrowseActivity (Navigation par catégorie)"
echo "  🎬 ChannelsGridActivity (Grille de chaînes)"
echo "  🌍 CountriesActivity (Filtrage par pays)"
echo "  💎 QualityFilterActivity (Filtrage par qualité)"
echo ""
echo "5. Prochaine étape: Rebuild de l'application"
echo ""
echo "Voulez-vous lancer le rebuild maintenant? (y/n)"
read -r response

if [[ "$response" =~ ^[Yy]$ ]]; then
    echo ""
    echo "🔨 Rebuild en cours..."
    echo ""
    ./gradlew clean assembleDebug installDebug
    
    echo ""
    echo "=================================================="
    echo "🎉 INSTALLATION TERMINÉE AVEC SUCCÈS!"
    echo "=================================================="
    echo ""
    echo "Votre nouvelle UI TV est prête! 📺✨"
    echo ""
    echo "Au lancement, vous verrez:"
    echo "  📺 LIVE TV"
    echo "  🎬 SÉRIES (Netflix, Prime Video, Disney+...)"
    echo "  🎥 FILMS"
    echo "  📻 RADIO"
    echo "  🌍 PAR PAYS (FR, AR, ES, IT, US...)"
    echo "  💎 PAR QUALITÉ (4K, Full HD, HD)"
    echo ""
    echo "Bon visionnage! 🍿"
else
    echo ""
    echo "Rebuild annulé. Lancez manuellement avec:"
    echo "  ./gradlew clean assembleDebug installDebug"
    echo ""
fi
