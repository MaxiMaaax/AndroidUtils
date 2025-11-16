# Supprimer le contour gris de l'icône

Le contour gris apparaît quand l'icône n'a pas de fond transparent. macOS ajoute automatiquement un fond gris pour les icônes avec fond opaque.

## Solution

Pour avoir une icône sans contour gris, **l'image PNG source doit avoir un fond transparent** (canal alpha).

### Option 1 : Utiliser le script (recommandé)

1. **Assurez-vous que votre PNG a un fond transparent**
   - Ouvrez votre image dans un éditeur (Preview, Photoshop, GIMP)
   - Supprimez le fond gris/blanc pour le rendre transparent
   - Sauvegardez en PNG avec transparence

2. **Convertissez l'image en .icns** :
   ```bash
   ./scripts/fix_icon_transparency.sh ~/Downloads/my_icon_transparent.png
   ```

3. **Rebuild l'application** :
   ```bash
   ./gradlew packageDmg
   ```

4. **Videz le cache d'icônes** :
   ```bash
   killall Finder && killall Dock
   ```

### Option 2 : Créer manuellement

Si votre PNG a un fond transparent, vous pouvez utiliser :

```bash
# Créer un iconset
mkdir -p src/main/resources/app/icon.iconset

# Générer toutes les tailles (en préservant la transparence)
for size in 16 32 128 256 512 1024; do
  sips -z $size $size votre_image.png --out src/main/resources/app/icon.iconset/icon_${size}x${size}.png
  if [ $size -ne 1024 ]; then
    sips -z $((size*2)) $((size*2)) votre_image.png --out src/main/resources/app/icon.iconset/icon_${size}x${size}@2x.png
  fi
done

# Convertir en .icns
iconutil -c icns src/main/resources/app/icon.iconset -o src/main/resources/app/icon.icns
rm -rf src/main/resources/app/icon.iconset
```

## Vérifier la transparence

Pour vérifier si votre PNG a un canal alpha :

```bash
sips -g hasAlpha votre_image.png
```

Si ça affiche `hasAlpha: yes`, votre image a de la transparence ✓

## Outils recommandés

- **Preview (macOS)** : Pour vérifier et éditer rapidement
- **GIMP** (gratuit) : Pour supprimer le fond et créer un PNG transparent
- **Photoshop** : Si vous l'avez
- **Remove.bg** (en ligne) : Pour supprimer automatiquement les fonds

Une fois que vous avez un PNG avec fond transparent, le script `fix_icon_transparency.sh` générera une icône .icns sans contour gris.




