# Image de fond du DMG

Pour personnaliser l'image de fond du DMG :

## Instructions

1. **Créez une image PNG** de 600x400 pixels (ou dimensions similaires)
   - Format recommandé : PNG avec transparence si nécessaire
   - Couleurs suggérées : 
     - Fond sombre (#1E1E1E) pour correspondre au thème de l'app
     - Accent Android vert (#3DDC84) ou violet (#6200EE)

2. **Placez l'image** dans :
   ```
   src/main/resources/app/dmg_background.png
   ```

3. **Build le DMG personnalisé** :
   ```bash
   ./gradlew customizeDmg
   ```

   Ou build normal (la tâche customizeDmg sera appelée automatiquement après packageDmg) :
   ```bash
   ./gradlew packageDmg
   ```

## Design suggéré

L'image de fond peut inclure :
- Logo ou symbole Android/ADB
- Nom de l'application
- Instructions visuelles (glisser l'app dans Applications)
- Design cohérent avec l'interface de l'application (thème sombre)

## Exemple de dimensions

- **Largeur** : 600-800 pixels
- **Hauteur** : 400-500 pixels
- **Format** : PNG (support transparence)

La fenêtre du DMG sera configurée automatiquement pour afficher cette image en arrière-plan.




