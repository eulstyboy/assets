# Pluie Vélo

Appli Android personnelle : pluie des deux prochaines heures là où vous êtes, pluie reçue
sur un trajet à vélo, équipement conseillé, widget et alerte quand la pluie arrive.
Aucun compte, aucune clé d'API, aucun serveur.

## Comment la prévision est faite

1. **Radar** — les 4 dernières images RainViewer (mosaïque des radars nationaux, une image
   toutes les 10 min, ~800 m par pixel). L'appli mesure le déplacement des averses par
   corrélation entre images, puis prolonge la dernière image le long de ce déplacement.
2. **Modèles** — précipitations au quart d'heure de cinq modèles via Open-Meteo :
   Météo-France AROME, DWD ICON, KNMI Harmonie, UK Met Office, ECMWF IFS.
3. **Croisement** — le radar pèse 90 % sur les 10 premières minutes, puis de moins en
   moins (30 % à 1 h, 0 % à 1 h 30) ; son poids baisse aussi quand le déplacement est mal
   établi. L'accord entre radar et modèles donne l'indice de confiance.

Pour un trajet, l'itinéraire vélo (OSRM / OpenStreetMap) est parcouru minute par minute à
votre vitesse : la pluie est lue à l'endroit et à l'heure où vous y serez, pour un départ
maintenant et toutes les 5 min pendant une heure.

Limites : en France il n'existe qu'un réseau radar (Météo-France) ; « croiser » veut donc
dire radar + modèles, pas plusieurs radars. L'échelle de couleurs RainViewer a été
reconstituée à partir des tuiles (une couleur par dBZ), puis convertie en mm/h par la loi
de Marshall-Palmer : les quantités restent des estimations.

## Compiler

    ANDROID_HOME=/chemin/du/sdk gradle :app:assembleDebug
    # APK : app/build/outputs/apk/debug/app-debug.apk

Tests : `gradle :app:testDebugUnitTest`. Ajouter `-Dlive=1` pour interroger les vrais
services (dont un test rétrospectif du radar), `-Dshots=1` pour produire les captures
d'écran dans `app/build/shots/`.
