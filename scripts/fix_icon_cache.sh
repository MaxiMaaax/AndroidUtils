#!/usr/bin/env bash
# Script pour vider le cache d'icônes macOS et forcer le rechargement

echo "Vidage du cache d'icônes macOS..."

# Vider le cache d'icônes
killall Finder 2>/dev/null || true
sudo rm -rf /Library/Caches/com.apple.iconservices.store 2>/dev/null || true
rm -rf ~/Library/Caches/com.apple.iconservices.store 2>/dev/null || true

# Vider le cache Dock
killall Dock 2>/dev/null || true

# Toucher le fichier de cache
touch /Applications/AndroidUtils.app 2>/dev/null || true

echo "Cache vidé. Redémarrez Finder et Dock pour voir les changements."
echo "Ou exécutez: killall Finder && killall Dock"




