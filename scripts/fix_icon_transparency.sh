#!/usr/bin/env bash
# Script pour créer une icône .icns avec fond transparent
# Nécessite que la source PNG ait un canal alpha (transparence)

set -e

ICON_DIR="src/main/resources/app"
ICONSET_DIR="$ICON_DIR/icon.iconset"

if [ -z "$1" ]; then
    echo "Usage: $0 <chemin_vers_image.png>"
    echo ""
    echo "IMPORTANT: L'image PNG source DOIT avoir un fond transparent (canal alpha)"
    echo "Si votre PNG a un fond gris/blanc, supprimez-le d'abord avec un éditeur d'image"
    echo ""
    echo "Exemples:"
    echo "  $0 ~/Downloads/my_icon.png"
    echo "  $0 src/main/resources/app/icon_source.png"
    exit 1
fi

SOURCE_IMAGE="$1"

if [ ! -f "$SOURCE_IMAGE" ]; then
    echo "Erreur: Fichier non trouvé: $SOURCE_IMAGE"
    exit 1
fi

echo "Vérification de la transparence de l'image..."
if ! sips -g hasAlpha "$SOURCE_IMAGE" | grep -q "hasAlpha: yes"; then
    echo ""
    echo "⚠ ATTENTION: L'image n'a pas de canal alpha (transparence)!"
    echo "L'icône aura un fond opaque (gris/blanc)."
    echo ""
    echo "Pour corriger cela:"
    echo "1. Ouvrez l'image dans un éditeur d'image (Photoshop, GIMP, Preview)"
    echo "2. Supprimez le fond gris/blanc pour le rendre transparent"
    echo "3. Sauvegardez en PNG avec transparence"
    echo "4. Réexécutez ce script"
    echo ""
    read -p "Continuer quand même? (o/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[OoYy]$ ]]; then
        exit 1
    fi
fi

echo "Conversion de $SOURCE_IMAGE en icône .icns avec transparence..."

# Créer le répertoire iconset
mkdir -p "$ICONSET_DIR"

# Générer toutes les tailles nécessaires
echo "Génération des différentes tailles..."

sizes=(16 32 128 256 512 1024)

for size in "${sizes[@]}"; do
    echo "  - Génération ${size}x${size}..."
    # Préserver la transparence avec sips
    sips -z $size $size "$SOURCE_IMAGE" --out "$ICONSET_DIR/icon_${size}x${size}.png" 2>/dev/null || true
    
    # Versions @2x pour Retina (sauf 1024)
    if [ $size -ne 1024 ]; then
        double_size=$((size * 2))
        echo "  - Génération ${double_size}x${double_size} (@2x)..."
        sips -z $double_size $double_size "$SOURCE_IMAGE" --out "$ICONSET_DIR/icon_${size}x${size}@2x.png" 2>/dev/null || true
    fi
done

# Convertir l'iconset en .icns (préserve la transparence)
echo "Conversion en fichier .icns..."
iconutil -c icns "$ICONSET_DIR" -o "$ICON_DIR/icon.icns"

if [ $? -eq 0 ]; then
    echo "✓ Icône créée avec succès : $ICON_DIR/icon.icns"
    echo "  (Fond transparent préservé)"
    echo ""
    echo "Vous pouvez maintenant builder le DMG avec:"
    echo "  ./gradlew packageDmg"
    
    # Nettoyer le répertoire temporaire
    rm -rf "$ICONSET_DIR"
    echo "✓ Répertoire temporaire nettoyé"
    
    # Forcer la mise à jour du cache
    echo ""
    echo "Pour voir les changements dans le Dock:"
    echo "  killall Finder && killall Dock"
else
    echo "✗ Erreur lors de la conversion"
    exit 1
fi




