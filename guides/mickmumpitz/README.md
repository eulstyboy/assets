# 🎬 La Quête du Cinéaste IA

**La méthode Mickmumpitz en 13 niveaux (+ le camp de base)**

> 🎮 **Version interactive** (XP, badges, quiz, progression sauvegardée) : ouvre [`index.html`](index.html) dans ton navigateur.

Mickmumpitz (Mick Mahler) est un artiste VFX allemand qui publie, avec chaque vidéo, un workflow **ComfyUI gratuit** pour faire du cinéma, de la 3D et des VFX avec des IA **locales et open source**. Ce guide condense ses 13 dernières vidéos (nov. 2025 → oct. 2026, environ 3 h 24 de tutoriels) en une progression façon jeu vidéo : coche les étapes pour gagner de l'XP, relève les défis pour débloquer des badges, et termine par le quiz. Ta progression est sauvegardée dans ton navigateur.

## 🎮 Règles du jeu

- Chaque **étape** cochée rapporte **10 XP**.
- Chaque **défi** relevé rapporte le bonus du niveau et débloque son **badge** (avec toutes les étapes cochées).
- Chaque bonne réponse au **quiz** rapporte **25 XP**, au premier essai seulement.
- L'ordre suit la difficulté, pas la date de sortie : commence au camp de base.

## 🗺️ La carte

| Niv. | Monde | Quête | Difficulté | XP de défi | Badge | GPU RunPod |
|---|---|---|---|---|---|---|
| 0 | Camp de base | [🏕️ Le Camp de base](#l0) | ★☆☆☆☆ | +50 | 🏕️ Campeur·euse | — |
| 0+ | Camp de base | [🚀 Le Hangar à GPU (RunPod)](#lr) | ★☆☆☆☆ | +75 | ☁️ Pilote de nuage | — |
| 1 | Monde 1 · La Salle de post-prod | [🔍 Le Zoom qui invente](#l1) | ★☆☆☆☆ | +100 | 🔍 Œil de lynx | RTX 4090 (24 Go) ([☁️](https://console.runpod.io/deploy?template=g6f7qbratd&ref=mgaruyku)) |
| 2 | Monde 2 · La Fabrique de personnages | [🧬 L'Usine à sosies](#l2) | ★★☆☆☆ | +150 | 🧬 Cloneur·euse | RTX 4090 ou RTX 5090 ([☁️](https://console.runpod.io/deploy?template=o1faf9xnyz&ref=mgaruyku)) |
| 3 | Monde 2 · La Fabrique de personnages | [🎬 Le Clap plan par plan](#l3) | ★★☆☆☆ | +150 | 🎬 Story-boardeur·euse | RTX 4090 ou RTX 5090 |
| 4 | Monde 3 · Le Plateau de tournage | [🎞️ Le Plateau modulaire](#l4) | ★★★☆☆ | +200 | 🎞️ Monteur·euse | RTX 5090 (32 Go) ou L40S (48 Go) ([☁️](https://console.runpod.io/deploy?template=x4px1sy09w&ref=mgaruyku)) |
| 5 | Monde 3 · Le Plateau de tournage | [🪄 Le Marionnettiste](#l5) | ★★★☆☆ | +200 | 🪄 Marionnettiste | RTX 4090 ou RTX 5090 ([☁️](https://console.runpod.io/deploy?template=hok2ttp6qw&ref=mgaruyku)) |
| 6 | Monde 4 · La Salle des machines (rendu 3D → IA) | [⚙️ Le Moteur de rendu 2.0](#l6) | ★★★☆☆ | +200 | ⚙️ Mécano du rendu | RTX 4090 (GGUF) ou L40S ([☁️](https://console.runpod.io/deploy?template=edjge1eypm&ref=mgaruyku)) |
| 7 | Monde 4 · La Salle des machines (rendu 3D → IA) | [🐻 Bear Minimum](#l7) | ★★★★☆ | +250 | 🐻 Gérant·e de motel | RTX 5090 ou L40S ([☁️](https://console.runpod.io/deploy?template=w6zqdu6zzz&ref=mgaruyku)) |
| 8 | Monde 4 · La Salle des machines (rendu 3D → IA) | [🗿 La Pâte qui prend vie](#l8) | ★★★☆☆ | +200 | 🗿 Sculpteur·rice | L40S (48 Go) ; RTX PRO 6000 pour le NVFP4 |
| 9 | Monde 5 · Le Studio VFX | [💥 Le Studio VFX de poche](#l9) | ★★★★☆ | +250 | 💥 Artificier·ère | RTX 4090/5090 en FP8, L40S pour être large ([☁️](https://console.runpod.io/deploy?template=hb55l4frnh&ref=mgaruyku)) |
| 10 | Monde 5 · Le Studio VFX | [🐷 Groin groin](#l10) | ★★★★☆ | +250 | 🐷 Cochon d'or | 24 Go et plus avec l'encodeur sur CPU ; 48 Go conseillés |
| 11 | Monde 6 · Au-delà de l'écran (3D & 4D) | [🗺️ Le Cartographe](#l11) | ★★★★★ | +300 | 🗺️ Cartographe | L40S (48 Go) |
| 12 | Monde 6 · Au-delà de l'écran (3D & 4D) | [🕶️ Bullet time maison](#l12) | ★★★★★ | +300 | 🕶️ L'Élu·e | L40S (48 Go) |
| 13 | Boss final | [👑 SHIBUWAKA : un anime avec un seul modèle](#l13) | ★★★★★ | +400 | 👑 Réalisateur·rice | RTX PRO 6000 (96 Go) ou H100 |

## 📜 Les Lois de Mick

Les principes qui reviennent d'une vidéo à l'autre.

1. 🧱 **La maquette d'abord, l'IA ensuite** : Bloque la mise en scène toi-même (Blender, jouets au bout d'un bâton, prévis, vidéo de téléphone) et laisse l'IA s'occuper du look. Mick cherche toujours le **contrôle**, pas le hasard.
2. 🪪 **L'identité vient des références, pas du prompt** : Prompt « vague mais ancré » (« le protagoniste, dans un champ à l'heure dorée ») et références visuelles fortes : un visage, une vue de face plus une vue de profil ou de 3/4. Tu pourras changer de héros sans réécrire les prompts.
3. 🎲 **La seed est ton dé** : Résultat presque bon ? Change la seed, pas tout le reste. Avec LTX (deux étapes), change d'abord la seed de l'étape 2.
4. 🐢 **Brouillon avant chef-d'œuvre** : Itère en basse résolution (576p/720p, scale 0.5×), avec un `frame_load_cap` réduit et les aperçus activés. Génère toutes les images fixes avant les vidéos.
5. 🧮 **Respecte les maths des frames** : Wan : `4n+1` frames (81, 121…). MiniMax H3 : 90, 107, 124… (pas de 17) jusqu'à 362. LTX : résolution divisible par 64. **24 fps** partout.
6. 🔊 **Un son propre ou rien** : Dialogue seul, nettoyé (pas de musique ni de bruits), au moins aussi long que le plan. Sinon le modèle invente de l'audio ou fait bouger la bouche sur chaque bruit.
7. 🗣️ **Dis ce que tu veux, pas ce que tu ne veux pas** : « no camera move » ne fait rien ; « one continuous take » marche. Et ne nomme jamais ce qui doit disparaître (casque, rig) dans le prompt : le modèle le dessinerait.
8. ⚖️ **Lis les licences avant de vendre** : Flux 2 Klein 9B est non commercial (le 4B est plus permissif), et une LoRA hérite de la licence de son modèle de base. Mick a remplacé SMPL-X (non commercial) par SAM 3D Body dans son pipeline 4D.

## 🧭 Quel niveau pour mon projet ?

| Ta situation | Va au |
|---|---|
| Je n'ai pas de carte NVIDIA (AMD, portable…) | [🚀 Niveau 0+](#lr) |
| J'ai une vieille vidéo floue ou compressée | [🔍 Niveau 1](#l1) |
| Je veux un personnage récurrent (et je peux entraîner une LoRA) | [🧬 Niveau 2](#l2) |
| J'écris une histoire, sans 3D, en images fixes | [🎬 Niveau 3](#l3) |
| Je veux des plans animés qui parlent, sans 3D | [🎞️ Niveau 4](#l4) |
| J'ai des jouets, des découpages ou une prévis simple | [🪄 Niveau 5](#l5) |
| J'ai une scène Blender et je veux un plan long, stylisé, sans dialogue | [⚙️ Niveau 6](#l6) |
| J'ai une scène Blender avec des personnages qui parlent | [🐻 Niveau 7](#l7) |
| J'ai un playblast clay et je veux un plan fini avec le son | [🗿 Niveau 8](#l8) |
| J'ai filmé un vrai plan et je veux ajouter ou remplacer des éléments | [💥 Niveau 9](#l9) |
| Je veux me transformer en créature en gardant mon jeu | [🐷 Niveau 10](#l10) |
| Je veux un décor 3D explorable à partir d'une image | [🗺️ Niveau 11](#l11) |
| Je veux filmer une personne en 4D (bullet time) | [🕶️ Niveau 12](#l12) |
| Je veux produire un anime complet, voix et musique comprises | [👑 Niveau 13](#l13) |

---

## Camp de base

<a id="l0"></a>

### Niveau 0 · 🏕️ Le Camp de base

**Difficulté** ★☆☆☆☆ · **Défi** +50 XP · **Badge** 🏕️ Campeur·euse

Avant toute quête, installe ton camp : ComfyUI à jour, le Manager, et le « rituel » que Mick répète dans presque chaque vidéo.

**🎒 Ton arsenal**

- ComfyUI + ComfyUI-Manager
- ComfyUI-Mickmumpitz-Nodes (présent dans presque tous ses workflows)
- Optionnel : SageAttention (Windows, jusqu'à 2× plus rapide sur Wan)
- Optionnel : un template RunPod si ton GPU est trop juste

**🗺️ La quête**

- [ ] Installer ComfyUI (Mick a un guide d'installation gratuit sur mickmumpitz.ai) et ComfyUI-Manager.
- [ ] Manager → **Update All**, puis redémarrer.
- [ ] Le rituel : glisser le `.json` sur le canvas → Manager → **Install Missing Custom Nodes** → Install all → redémarrer → `Ctrl+Shift+R` dans le navigateur.
- [ ] Télécharger les modèles dans les bons dossiers `models/…` (liens et dossiers sont dans les notes « START HERE » à gauche de chaque workflow).
- [ ] Ouvrir chaque loader et vérifier que le nom de fichier correspond **exactement** (touche `R` pour rafraîchir la liste).

**💡 Astuces de Mick**

- Astuce de Mick : colle le guide d'installation dans ChatGPT ou Claude Code et laisse-le tout installer pour toi.
- Petit GPU : cherche les versions **GGUF**, `w4a8` ou `int8` des modèles, ou utilise les templates RunPod fournis avec chaque workflow.

> 🪤 **Piège :** **RES4LYF** ne s'installe parfois pas via « Install Missing » : installe-le à la main depuis la liste des custom nodes du Manager. Nœuds Mickmumpitz manquants ? « Try Update » ou « Switch Version » sur 1.3.2.

- [ ] ⚔️ **Défi (+50 XP) :** Faire tourner un petit workflow simple de bout en bout avant d'attaquer les gros.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/comfyui-getting-153738117)</sub>

<a id="lr"></a>

### Niveau 0+ · 🚀 Le Hangar à GPU (RunPod)

**Difficulté** ★☆☆☆☆ · **Défi** +75 XP · **Badge** ☁️ Pilote de nuage

Pas de carte NVIDIA (AMD, portable, petite carte) ? Loues-en une **à la seconde** sur RunPod : ComfyUI tourne sur leur machine et tu l'utilises dans ton navigateur. Mick fournit des templates prêts à l'emploi (nœuds déjà installés) pour 7 niveaux : cherche le lien ☁️ sur les cartes.

**🎒 Ton arsenal**

- Un compte RunPod et environ 10 $ de crédit
- Le template du niveau (lien ☁️ sur la carte)
- Optionnel mais conseillé : un *network volume* de 100 à 150 Go (environ 0,07 $ par Go et par mois)
- Un compte Hugging Face pour les modèles « gated » (Flux 2 Klein)

**🗺️ La quête**

- [ ] Créer un compte sur runpod.io et ajouter du crédit (10 $ suffisent pour démarrer).
- [ ] Optionnel : créer un **network volume** dans le datacenter où tu loueras ton GPU. Modèles, nœuds et rendus survivent alors à la suppression du pod.
- [ ] Ouvrir le lien ☁️ du niveau, choisir le GPU conseillé (Community Cloud = moins cher) et déployer.
- [ ] Attendre la fin de l'installation (le premier démarrage peut prendre 20 à 30 minutes), puis **Connect** → ouvrir ComfyUI (port 8188).
- [ ] Télécharger le `.json` du workflow sur le post de Mick (la version « runpod » quand il y en a une) et le glisser sur le canvas.
- [ ] Lancer, puis récupérer le rendu (clic droit → Save, ou le dossier `output`).
- [ ] **Arrêter ou supprimer le pod** dès que tu as fini.

**💡 Astuces de Mick**

- Facturation à la seconde. **Community Cloud** coûte moins cher mais la disponibilité varie ; **Secure Cloud** est plus cher et plus fiable.
- Les liens de templates de Mick sont des liens d'affiliation : selon ses posts, après tes 10 premiers dollars, vous recevez tous les deux un bonus de crédit (5 $ fixes dans l'UE).
- Modèle « gated » : accepte la licence sur Hugging Face, puis `hf auth login` dans le terminal JupyterLab du pod (voir le niveau 2).
- Plus tard, Claude peut piloter le ComfyUI du pod via son API (adresse du type `https://<id-du-pod>-8188.proxy.runpod.net`), si tu autorises l'accès réseau et la clé.

> 🪤 **Piège :** Un pod oublié **continue de facturer**, même s'il ne calcule rien : vérifie la page *Pods* à la fin de chaque session. Un pod arrêté (*Stop*) facture encore un peu son disque ; *Terminate* libère tout, donc télécharge tes rendus avant si tu n'as pas de volume.

- [ ] ⚔️ **Défi (+75 XP) :** Lancer le template de l'upscaler (niveau 1) sur une RTX 4090, upscaler un clip, et supprimer le pod en moins d'une heure.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://docs.runpod.io/tutorials/pods/comfyui)</sub>

---

## Monde 1 · La Salle de post-prod

<a id="l1"></a>

### Niveau 1 · 🔍 Le Zoom qui invente

**Difficulté** ★☆☆☆☆ · **Défi** +100 XP · **Badge** 🔍 Œil de lynx

🎥 [STOP Paying for AI Upscaling - This FREE Tool is BETTER [ComfyUI Workflow + Tutorial]](https://www.youtube.com/watch?v=NpNagmQI4yg) · 25 nov. 2025 · 9:16 · ~173 k vues

🖥️ GPU conseillé : **RTX 4090 (24 Go)** · [☁️ Template RunPod de Mick](https://console.runpod.io/deploy?template=g6f7qbratd&ref=mgaruyku)

Un upscaler **génératif** (Wan 2.2 + Ultimate SD Upscaler) qui crée du vrai détail jusqu'en HD, 2K ou 4K au lieu d'interpoler des pixels. Selon Mick, il bat souvent Topaz, et son équipe l'utilise sur presque tous ses projets. C'est le niveau tutoriel : on règle, on appuie sur Run.

**🎒 Ton arsenal**

- Wan 2.2 + LoRA lightning (mise à jour en v1.1)
- Ultimate SD Upscaler
- Sampler/scheduler RES4LYF
- Découpage automatique en lots (Batch Creator)

**🗺️ La quête**

- [ ] Charger la vidéo et régler **Target Resolution** (largeur, hauteur).
- [ ] Choisir la qualité : `False` = 1 step (rapide), `True` = 2 steps (haute qualité, plus lent).
- [ ] Régler **Creativity** : bas = fidèle à la source, haut = plus de détails inventés.
- [ ] Mettre **Frames per Iteration** sous la longueur du clip pour activer le découpage automatique, et **Overlap Frames** à 16 (bon compromis pour les raccords).
- [ ] Lancer. Le prompt est généré automatiquement (tu peux ajouter tes propres lignes).

**💡 Astuces de Mick**

- Longues séquences en 4K : mets **Save Image Sequence** à `True` pour éviter les soucis de mémoire, puis réassemble dans Resolve ou After Effects.
- Ne touche pas aux sous-graphes Batch Creator / Blend : ce ne sont que des maths.

> 🪤 **Piège :** Une Creativity trop élevée crée des artefacts de bruit.

- [ ] ⚔️ **Défi (+100 XP) :** Prendre une vieille vidéo compressée (480p) et la passer en 2K, puis comparer avant/après côte à côte.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/stop-paying-for-154393504)</sub>

---

## Monde 2 · La Fabrique de personnages

<a id="l2"></a>

### Niveau 2 · 🧬 L'Usine à sosies

**Difficulté** ★★☆☆☆ · **Défi** +150 XP · **Badge** 🧬 Cloneur·euse

🎥 [Create HYPERREALISTIC AI Characters That INTERACT | FREE & LOCAL](https://www.youtube.com/watch?v=ghFYDG0DF1w) · 17 juil. 2026 · 34:28 · ~75 k vues

🖥️ GPU conseillé : **RTX 4090 ou RTX 5090** · [☁️ Template RunPod de Mick](https://console.runpod.io/deploy?template=o1faf9xnyz&ref=mgaruyku)

**Consistent Character Creator 4.0** : une seule image de référence → un dataset complet du personnage (T-pose, marche de profil, gros plans, tout le board). Tu ne gardes que les images réussies, tu les légendes, puis tu entraînes ton casting dans **une LoRA**.

**🎒 Ton arsenal**

- Flux 2 Klein 9B (fp8)
- Text encoder `qwen_3_8b_fp8mixed`
- `flux2-vae`
- LoRA `Flux2-Klein-9B-consistency-V2` (réglée à 0.35)
- 7 packs de nœuds seulement (version simple)

**🗺️ La quête**

- [ ] Charger `260713_MICKMUMPITZ_CCC_4-0_SMPL.json` et faire le rituel.
- [ ] Télécharger les 4 modèles. Laisser le type de CLIP sur `flux2` et la LoRA à 0.35.
- [ ] Charger ta référence et donner le **nom du personnage** dans OUTPUT NAMING *maintenant* (Mick : renommer 60 images plus tard n'a rien d'une soirée amusante).
- [ ] **Save Dataset** : `2` pour prévisualiser, `1` pour sauvegarder. Queue Prompt.
- [ ] Trier : ne garder que les images qui ressemblent vraiment au personnage (dans `ComfyUI/output/CCC/<NOM>/`).
- [ ] Étape suivante : légender avec le workflow **DATASET-TAGGER**, puis entraîner la LoRA.

**💡 Astuces de Mick**

- Décris le personnage dans tes prompts pour garder le dataset cohérent.
- Sur RunPod, le modèle est « gated » : accepte la licence sur Hugging Face, puis `hf auth login` et `hf download` directement sur le pod (bien plus rapide que par le navigateur).

> 🪤 **Piège :** **Licences !** Flux 2 Klein 9B est non commercial, et ta LoRA en hérite même si le fichier est à toi. Pour vendre : la version 3.5 ou la version Qwen Image Edit (licence plus permissive). Mick prépare une version basée sur Krea 2 (usage commercial gratuit sous 50 personnes).

- [ ] ⚔️ **Défi (+150 XP) :** Créer un personnage original à partir d'un croquis ou d'une photo, générer son board complet et garder au moins 20 images validées.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/new-video-free-4-164075060)</sub>

<a id="l3"></a>

### Niveau 3 · 🎬 Le Clap plan par plan

**Difficulté** ★★☆☆☆ · **Défi** +150 XP · **Badge** 🎬 Story-boardeur·euse

🎥 [Build your own AI MOVIES shot by shot - FREE & LOCAL! [AI Filmmaking in ComfyUI - 2025]](https://www.youtube.com/watch?v=YpuSE9hcal8) · 1 nov. 2025 · 17:00 · ~203 k vues

🖥️ GPU conseillé : **RTX 4090 ou RTX 5090** · pas de template RunPod de Mick (template ComfyUI générique)

Des films plan par plan avec personnages et décors cohérents, **sans entraîner de LoRA**. On déplace la caméra dans des environnements à 360°, on change de personnage ou de lieu instantanément, on dirige avec de simples prompts. Tous les styles marchent : réaliste, anime, pâte à modeler.

> ℹ️ Le post compagnon de cette vidéo est court : les étapes ci-dessus résument la logique décrite ; le pas-à-pas détaillé est dans la vidéo.

**🎒 Ton arsenal**

- Qwen Image Edit
- LoRA **Next Scene** (communauté)
- LoRA 360° maison de Mick pour les plans d'environnement

**🗺️ La quête**

- [ ] Récupérer le workflow et la LoRA 360° de Mick (les autres modèles sont listés dans les notes du workflow).
- [ ] Générer un environnement à 360° à partir d'une image de décor.
- [ ] Cadrer chaque plan en déplaçant ta caméra dans ce décor 360°.
- [ ] Ajouter ton personnage de référence et décrire l'action du plan en texte.
- [ ] Enchaîner les plans avec Next Scene pour garder la continuité.

**💡 Astuces de Mick**

- Change l'image du personnage ou du décor pour réutiliser toute ta séquence avec un autre casting.
- Cette méthode est l'ancêtre du Movie Builder LTX 2.3 (niveau 4), qui ajoute la vidéo et les voix.

> 🪤 **Piège :** Un lien de modèle erroné a été corrigé le 3 nov. 2025 : télécharge toujours la dernière version du workflow.

- [ ] ⚔️ **Défi (+150 XP) :** Un storyboard de 6 plans dans un même décor 360°, puis remplacer le héros par un autre personnage sans réécrire les prompts.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/new-video-build-142571395)</sub>

---

## Monde 3 · Le Plateau de tournage

<a id="l4"></a>

### Niveau 4 · 🎞️ Le Plateau modulaire

**Difficulté** ★★★☆☆ · **Défi** +200 XP · **Badge** 🎞️ Monteur·euse

🎥 [Generate ENTIRE AI MOVIES with this NEW METHOD! [FREE & LOCAL]](https://www.youtube.com/watch?v=0mT4p86ZxGQ) · 4 mai 2026 · 10:43 · ~75 k vues

🖥️ GPU conseillé : **RTX 5090 (32 Go) ou L40S (48 Go)** · [☁️ Template RunPod de Mick](https://console.runpod.io/deploy?template=x4px1sy09w&ref=mgaruyku)

Le **Movie Builder** : une référence personnage, une référence décor, un échantillon de voix et un prompt → image de départ (Flux 2 Klein) → vidéo avec dialogue synchronisé (LTX 2.3) → plan suivant. Rien n'est figé : change l'image du héros et tout le film change de héros.

**🎒 Ton arsenal**

- LTX 2.3 (vidéo + audio, distillé en 2 étapes)
- Flux 2 Klein 9B (ou 4B, licence plus permissive)
- LoRA `ltx-2.3-id-lora-talkvid` (clonage de voix)
- Upscaler spatial ×2
- ShotDuplicator / ShotAssembler (Mickmumpitz Nodes)

**🗺️ La quête**

- [ ] Groupe **Settings** : image à 1152 px, vidéo en 720p pour itérer, 24 fps.
- [ ] **Load Protagonist Images** : un gros plan du visage plus un plein pied de 3/4. Le sous-graphe produit FACE, BODY et FACEBODY.
- [ ] **Audio Reference 01** : un échantillon de voix propre de 5 secondes.
- [ ] Dans chaque plan : FACEBODY dans l'entrée 1, un prompt vague mais ancré, puis ▶ sur Save Image pour générer seulement l'image.
- [ ] Partie vidéo : durée en secondes, interrupteur **voix** activé pour les dialogues, réplique dans le champ dialogue, ▶ sur Save Video.
- [ ] **Shot Duplicator → Add Shot** pour chaque nouveau plan (Color Match sur le plan 01 câblé automatiquement). L'ordre de la shotlist = l'ordre du montage.
- [ ] **Final Movie Output** : le ShotAssembler assemble le film. Donne un *Folder label* unique (draft_01, draft_02…).

**💡 Astuces de Mick**

- Mauvaise voix ? Change d'abord la seed de l'étape 2, puis celle de l'étape 1.
- Utilise le toggle du haut pour générer **toutes les images d'abord** : tu évites de charger et décharger Flux et LTX à chaque plan.
- Les 4 entrées de référence sont agnostiques : personnage, accessoire, décor… Les groupes **Asset Creator** fabriquent les accessoires.
- Le décor change quand tu zoomes ? Ajoute l'image du plan précédent comme référence.

> 🪤 **Piège :** Dupliquer un plan à la main au lieu d'utiliser le Shot Duplicator : le Color Match n'est pas câblé et l'étalonnage saute d'un plan à l'autre.

- [ ] ⚔️ **Défi (+200 XP) :** Un court de 4 plans avec une réplique, puis la même histoire avec un autre héros en changeant seulement l'image de référence et la voix.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/guide-ltx-2-3-157329723)</sub>

<a id="l5"></a>

### Niveau 5 · 🪄 Le Marionnettiste

**Difficulté** ★★★☆☆ · **Défi** +200 XP · **Badge** 🪄 Marionnettiste

🎥 [AI is finally controllable! …with a stick [FREE ComfyUI Workflow + Tutorial]](https://www.youtube.com/watch?v=pUb58eAZ3pc) · 17 déc. 2025 · 10:40 · ~124 k vues

🖥️ GPU conseillé : **RTX 4090 ou RTX 5090** · [☁️ Template RunPod de Mick](https://console.runpod.io/deploy?template=hok2ttp6qw&ref=mgaruyku)

Tu bouges des jouets ou des découpages au bout d'un **bâton** dans ton décor ; le workflow efface le bâton et anime l'objet en suivant exactement ton geste, avec ombres, reflets et interactions. C'est basé sur le papier de recherche **Time-to-Move** (sans entraînement), qui marche le mieux avec Wan 2.2. Mick en a tiré un court-métrage de jouets qui prennent vie.

**🎒 Ton arsenal**

- Wan 2.2 + Time-to-Move (TTM latents)
- Qwen Image Edit pour effacer le bâton
- SAM3 (ou SAM2) pour le masque du sujet

**🗺️ La quête**

- [ ] Workflow de **préparation** : charger la vidéo (rester en 720p) et donner un `SET SHOTNUMBER` unique à chaque plan.
- [ ] Récupérer la première et la dernière image : clic droit → Copy Image, puis `Ctrl+V` dans un Load Image, et peindre le masque d'inpainting.
- [ ] **Qwen Remove** : prompt « remove the wooden stick » (autre seed ou masque plus large si ça résiste).
- [ ] **SAM3** : choisir l'image où le sujet est le plus visible, clic gauche sur le sujet, clic droit pour des points négatifs sur le bâton.
- [ ] Workflow **Time2Move** : charger l'image de début et/ou de fin, plus la vidéo d'entrée et la vidéo masque (ces deux-là sont obligatoires).
- [ ] Résolution : `1` = 576p, `2` = 720p, `3` = personnalisée. Commencer par un prompt court (« A plush dinosaur jumps »).

**💡 Astuces de Mick**

- Environ **70 %** des plans du court ont marché du premier coup.
- Pas de bâton ? Une prévis animée dans Blender ou After Effects sert aussi de référence de mouvement.
- Pour se rajeunir avec Wan Animate, Mick a dû entraîner une LoRA sur ses photos d'enfance : sans elle, il changeait de visage à chaque plan.

> 🪤 **Piège :** `WanVideo Add TTMLatents` est réglé sur `end_step 2`. À 3, le résultat suit plus strictement ta vidéo, mais l'équipe trouvait les rendus moins beaux.

- [ ] ⚔️ **Défi (+200 XP) :** Faire voler une peluche à travers ta pièce avec un bâton, caméra à la main.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/ai-video-with-154392669)</sub>

---

## Monde 4 · La Salle des machines (rendu 3D → IA)

<a id="l6"></a>

### Niveau 6 · ⚙️ Le Moteur de rendu 2.0

**Difficulté** ★★★☆☆ · **Défi** +200 XP · **Badge** ⚙️ Mécano du rendu

🎥 [We Built an AI Render Engine for FREE](https://www.youtube.com/watch?v=0WkixvqnPXw) · 25 févr. 2026 · 15:30 · ~182 k vues

🖥️ GPU conseillé : **RTX 4090 (GGUF) ou L40S** · [☁️ Template RunPod de Mick](https://console.runpod.io/deploy?template=edjge1eypm&ref=mgaruyku)

**AI Renderer 2.0** : exporte des passes de profondeur et de contours depuis Blender, fabrique une image de référence avec Z-Image Turbo, et le merge **SkyReels V3 × Wan VACE** rend une vidéo stylisée qui suit exactement ta géométrie : textures, lumière, fumée, eau, débris.

**🎒 Ton arsenal**

- Blender (passes line art + depth)
- Z-Image Turbo + ControlNet (image de départ)
- Merge SkyReels V3 R2V × Wan VACE (versions GGUF depuis le 23/03/2026)
- Méthode Phantom : jusqu'à 4 références

**🗺️ La quête**

- [ ] Exporter de Blender une passe **line art** et une passe **depth** en vidéo.
- [ ] Charger 1 ou 2 vidéos de contrôle ; activer **Blending** : Mick utilise line art + 20 % de depth (la line art seule suffit souvent).
- [ ] Créer l'image de départ (le style) avec le workflow Z-Image Turbo.
- [ ] Une seule image = méthode *start image* ; plusieurs images = méthode **Phantom** (personnage et décor séparés, jusqu'à 4).
- [ ] Écrire le prompt et lancer (jusqu'à environ 121 frames en une passe).

**💡 Astuces de Mick**

- Version avancée pour les clips longs : `frames_per_iteration` en 4n+1, `num_start_frames` autour de 14.
- Nombre d'itérations = frames totales ÷ (frames_per_iteration − num_start_frames). `resume_from_iteration` permet de refaire un seul segment.
- Change la référence de style pour changer instantanément la direction artistique.

> 🪤 **Piège :** Version avancée : **un seul job à la fois**. Plusieurs jobs avec des entrées différentes « déteignent » les uns sur les autres dans la chaîne d'itérations.

- [ ] ⚔️ **Défi (+200 XP) :** Bloquer une scène en cubes dans Blender (5 minutes maximum), puis la rendre dans deux styles différents en changeant seulement l'image de référence.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/ai-renderer-2-0-153434545)</sub>

<a id="l7"></a>

### Niveau 7 · 🐻 Bear Minimum

**Difficulté** ★★★★☆ · **Défi** +250 XP · **Badge** 🐻 Gérant·e de motel

🎥 [We Built a FREE AI Render Engine for CG & Facial Animation (ComfyUI + Blender)](https://www.youtube.com/watch?v=7J7hi-Hxpac) · 19 juin 2026 · 23:00 · ~67 k vues

🖥️ GPU conseillé : **RTX 5090 ou L40S** · [☁️ Template RunPod de Mick](https://console.runpod.io/deploy?template=w6zqdu6zzz&ref=mgaruyku)

Le pipeline du court *Bear Minimum*, un motel tenu par une famille d'ours. Tu maquettes en 3D, une image Flux 2 Klein fixe le style, et **LTX 2.3** rend le plan animé **et synchronisé sur les lèvres** à partir d'un fichier audio. Vidéo sponsorisée par NVIDIA ; les workflows restent gratuits.

**🎒 Ton arsenal**

- Flux 2 Klein 9B + LoRA consistency (0.7)
- LTX 2.3 22B (dev + distilled, fp8) + Gemma 3 12B
- IC-LoRA union-control **19B** + IC-LoRA detailer
- NVIDIA RTX Video Super Resolution (upscale 4K)

**🗺️ La quête**

- [ ] Depuis Blender, rendre en vidéo : passe **depth** (blanc = proche), passe **canny/layout** (moteur Workbench, MatCap, Cavity + Outlines), **masque bouche** (noir = l'IA peut animer) et un **MP3** de dialogue seul.
- [ ] Étape 1, Flux Klein : layout + depth en entrée, jusqu'à 4 références, un prompt qui dit qui est où, puis le style et la lumière (1920×1088). Itérer jusqu'à l'image guide parfaite.
- [ ] Étape 2, LTX : résolution **divisible par 64** (1408×768, 1920×1088) et FRAME LOAD CAP = longueur du plan.
- [ ] Charger jusqu'à 3 images guides (début, milieu, fin) à leur position exacte, force 0.75 à 0.9.
- [ ] Charger depth, canny, masque bouche et voix ; mettre **USE MASK VIDEO** à true pour le lip sync.
- [ ] Prompt : décrire ce qui se passe, dans l'ordre, en insistant sur les expressions. Queue.
- [ ] Étape 3 : passer en 4K avec RTX Video Super Resolution (quelques secondes, sur toute carte RTX).

**💡 Astuces de Mick**

- Active les aperçus : souvent, dès le premier step, on voit si le plan marche. Stop et corrige.
- Baisser la force du ControlNet laisse LTX inventer davantage : bien pour un mouvement simple, mais ça casse les animations complexes.
- Le masque bouche : plus la zone noire est grande, plus le visage s'anime.

> 🪤 **Piège :** L'audio doit durer **au moins** autant que le plan, sinon le modèle invente du son après la voix. Et prends bien la LoRA union-control **19B**, pas la 22B : elle marche mieux avec les nœuds de Mick.

- [ ] ⚔️ **Défi (+250 XP) :** Un plan de dialogue de 5 s : un personnage low-poly qui dit une réplique enregistrée avec ton téléphone.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/new-video-free-161509029)</sub>

<a id="l8"></a>

### Niveau 8 · 🗿 La Pâte qui prend vie

**Difficulté** ★★★☆☆ · **Défi** +200 XP · **Badge** 🗿 Sculpteur·rice

🎥 [This new AI Render Engine is amazing!](https://www.youtube.com/watch?v=Q2oVBVQNQ_U) · 12 août 2026 · 14:09 · ~68 k vues

🖥️ GPU conseillé : **L40S (48 Go) ; RTX PRO 6000 pour le NVFP4** · pas de template RunPod de Mick (template ComfyUI générique)

Un playblast gris en « clay » + une description du look = **un plan fini, avec le son**. Avec **MiniMax H3**, plus de pile de ControlNet, ni de passe depth, ni d'OpenPose : selon Mick, ça fait ce que son équipe contournait depuis trois ans.

**🎒 Ton arsenal**

- MiniMax H3 `ref2va` int8 (21 Go) ou `w4a8` (11,8 Go)
- Text encoder Qwen3-VL 32B
- 2 VAE : image + son
- Turbo LoRA (4 steps au lieu de 20)
- SeedVR2 (version avancée)

**🗺️ La quête**

- [ ] Prendre le modèle **ref2va** (pas fl2va) et les **deux** VAE.
- [ ] Groupe 1 : vérifier que les 4 loaders trouvent leur fichier.
- [ ] Préparer la vidéo clay : **24 fps**, environ 1 mégapixel (1920×1080 convient), longueur parmi 90, 107, 124, 141… jusqu'à 362 frames (~15 s).
- [ ] Groupe 4 : charger CLAY VIDEO, puis une **LOOK PLATE** (image fixe de matière, couleur, lumière). Sélectionner le nœud et `Ctrl+B` pour l'activer (jusqu'à 3).
- [ ] Groupe 5 : prompt avec les balises `<Video 1>` et `<Picture 1>`, la description du look, un bloc **Timeline** qui va jusqu'à la fin du clip et une ligne **Audio**.
- [ ] Groupe 3 : seed, 4 steps avec la Turbo LoRA, `aspect_ratio` sur « From reference ». Run.

**📜 Parchemin : le modèle de prompt H3 (condensé)**

```text
<Video 1> is an untextured grey clay render, it defines layout, staging and animation.
Follow the composition, subject positions and timing of <Video 1> exactly, frame by frame.
Keep the geometry, layout and animation unchanged and give them material, colour and
lighting from the description below.
[Décris la scène : qui, tenues, environnement, heure, lumière, look film]
Timeline:
[0s-6.5s] [ce qui se passe, jusqu'à la fin du clip]
The camera move comes entirely from <Video 1>. One continuous take.
Audio: [les sons voulus, présents sur tout le clip]
Render every frame fully. No on-screen text.
```

**💡 Astuces de Mick**

- Décris le **look**, pas la mise en scène : la mise en scène vient de la vidéo.
- Ta propre voix : complète l'audio avec du silence jusqu'à la longueur du clip, mets la réplique entre guillemets avec « once and only once », et fais deux rendus (le début de la parole bouge avec la seed).
- Scale 0.5× ou 0.75× pour des brouillons rapides ; 1.0× est la taille d'entraînement du modèle.

> 🪤 **Piège :** Un clip de 150 frames est rendu en **141** : le reste est coupé. Et un clip qui n'est pas à 24 fps fait dériver le mouvement.

- [ ] ⚔️ **Défi (+200 XP) :** Animer une balle qui rebondit en clay dans Blender et la rendre dans 3 looks (une look plate différente à chaque fois), avec le bruitage.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/new-video-free-166463119)</sub>

---

## Monde 5 · Le Studio VFX

<a id="l9"></a>

### Niveau 9 · 💥 Le Studio VFX de poche

**Difficulté** ★★★★☆ · **Défi** +250 XP · **Badge** 💥 Artificier·ère

🎥 [I Built a FREE VFX Pipeline with Local AI [2026 Masterclass]](https://www.youtube.com/watch?v=_n0ir5V5tX4) · 30 mars 2026 · 19:12 · ~205 k vues

🖥️ GPU conseillé : **RTX 4090/5090 en FP8, L40S pour être large** · [☁️ Template RunPod de Mick](https://console.runpod.io/deploy?template=hb55l4frnh&ref=mgaruyku)

**AI VFX 1.0** : masque une zone de ta vidéo et remplace-la par du contenu IA qui suit précisément la géométrie de la scène. Le cœur : le merge d'Inner-Reflections, **SkyReels V3 R2V** (compréhension des références) × **Wan VACE** (ControlNet et inpainting). Quatre ingrédients à doser : masques, ControlNets, images de référence, prompts.

**🎒 Ton arsenal**

- Merge `wan-14B_vace_skyreels_v3_R2V` : FP8 (24 Go et plus) ou GGUF Q4_K_M
- RES4LYF
- Préprocesseur : SAM3, DepthCrafter, Canny, DWPose, CoTracker

**🗺️ La quête**

- [ ] **Préprocesseur** : charger la vidéo source (720p). Il sort masque, depth, canny, pose, tracking, plaque grise et image de départ.
- [ ] Sous « CHOOSE HERE! », choisir quelle donnée de ControlNet part dans la driving video (tracking par défaut).
- [ ] Workflow principal : brancher le loader FP8 ou GGUF sur le nœud LoRA, et commencer en 576p.
- [ ] Charger **DRIVING VIDEO** et **MASK** (même longueur, même fps).
- [ ] Ajouter une image de départ et/ou jusqu'à 4 références (pour changer un personnage : au moins une vue de face et une de profil ou de dos).
- [ ] Sampler : prompt détaillé, 5 steps, mode *Start + Reference*, *Reference* ou *Auto*. Activer la comparaison côte à côte.

**💡 Astuces de Mick**

- **Canny** garde la géométrie exacte ; **depth** garde la composition mais change tout le reste ; **pose** sert à remplacer un personnage ; le **tracking** (carrés blancs) fait comprendre le mouvement de caméra.
- Version avancée : un prompt et une seed par itération, frames en 4n+1, `resume_from_iteration`, et Color Match à désactiver si l'environnement change beaucoup.
- After Effects, Fusion ou Nuke restent recommandés pour la driving video et le compositing final, mais ne sont pas obligatoires.

> 🪤 **Piège :** Le post et le guide se contredisent sur la couleur du masque. Le guide détaillé dit **noir = garder, blanc = remplacer**. Fais un test court avant un long rendu.

- [ ] ⚔️ **Défi (+250 XP) :** Remplacer le ciel ou le décor derrière toi sur un plan caméra à la main de 3 s, sans After Effects.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/free-guide-epic-154092216)</sub>

<a id="l10"></a>

### Niveau 10 · 🐷 Groin groin

**Difficulté** ★★★★☆ · **Défi** +250 XP · **Badge** 🐷 Cochon d'or

🎥 [I turned myself into a pig with AI [Free & Local VFX Tutorial]](https://www.youtube.com/watch?v=2MzCkYrvDaI) · 7 oct. 2026 · 11:30 · ~31 k vues

🖥️ GPU conseillé : **24 Go et plus avec l'encodeur sur CPU ; 48 Go conseillés** · pas de template RunPod de Mick (template ComfyUI générique)

La **performance capture** locale avec MiniMax H3 : deviens n'importe quelle créature, ou une autre personne, avec une seule caméra. H3 ne régénère que la zone masquée : le mouvement de caméra reste verrouillé, ton corps et la pièce restent réels, et l'audio est ton vrai enregistrement. La créature joue comme toi : voix, bouche, expressions, mouvements de tête.

**🎒 Ton arsenal**

- MiniMax H3 `ref2va` + Turbo LoRA 8 steps
- LoRA Character Swap (communauté)
- SAM 3.1 (masques à partir de mots)
- ControlNet Union 2.0 (pose), H3-FaceRefine, DWPose
- ComfyUI 0.37 ou plus, GPU de 16 Go minimum (testé à partir de 24 Go), RAM de 32 Go et plus

**🗺️ La quête**

- [ ] Filmer en paysage, **avec son**, 15 s maximum. Nettoyer l'audio : H3 fait bouger la bouche sur chaque bruit.
- [ ] **BODY CLIP** : charger la prise (baisser `frame_load_cap` pour tester les premières secondes).
- [ ] **CREATURE SHEET** : face + profil avec tes vêtements, plus quelques têtes aux expressions exagérées. Sinon, activer NO CHARACTER SHEET et décrire la créature.
- [ ] **WHAT GETS REPLACED** : les mots que SAM cherche (« head » ou « person », + « helmet, phone, camera rig on the helmet »). Vérifier le PREVIEW.
- [ ] **PROMPT** : la créature et son jeu, en langage naturel (modèles fournis dans `PROMPT_TEMPLATES.txt`). Inutile d'écrire le dialogue : ta voix pilote la bouche.
- [ ] Run. Vérifier les aperçus avant d'attendre le rendu ; une vidéo comparative entrée/sortie est sauvegardée.

**💡 Astuces de Mick**

- Une caméra suffit. La deuxième sur le visage (le casque bricolé de la vidéo) n'aide que pour les plans larges ou quand tu te détournes.
- Réglage *head-size* contre l'effet « tête à ressort ». Plusieurs personnes : nomme celle qui joue et règle PERSON (0 = la plus à gauche).
- Moins de 32 Go de VRAM : CLIP loader sur `cpu`. Carte antérieure aux RTX 50 : text encoder `int8` au lieu de `nvfp4`.

> 🪤 **Piège :** **Ne nomme jamais ton casque ni ton rig dans le prompt** : ça pousse H3 à les dessiner (ils vont seulement dans WHAT GETS REPLACED). Et un clip sans son arrête le run.

- [ ] ⚔️ **Défi (+250 XP) :** Te transformer en animal qui récite une tirade de 10 s, en plan fixe, avec une seule caméra.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/new-video-free-171702311)</sub>

---

## Monde 6 · Au-delà de l'écran (3D & 4D)

<a id="l11"></a>

### Niveau 11 · 🗺️ Le Cartographe

**Difficulté** ★★★★★ · **Défi** +300 XP · **Badge** 🗺️ Cartographe

🎥 [We Open Sourced World Generation](https://www.youtube.com/watch?v=eJuYBNrD8HI) · 25 août 2026 · 17:55 · ~149 k vues

🖥️ GPU conseillé : **L40S (48 Go)** · pas de template RunPod de Mick (template ComfyUI générique)

Une image ou un prompt → **un monde 3D complet** où se promener, en local. La base est **Matrix-3D** de Skywork : panorama 360° → mesh grossier tiré de la profondeur → une caméra qui vole dedans → Wan 2.1 bouche les trous. La version officielle ne tourne que sous Linux ; l'équipe de Mick l'a reconstruite en nœuds ComfyUI, accélérée avec lightx2v, et dotée d'un éditeur de trajectoire de drone.

**🎒 Ton arsenal**

- Matrix-3D (LoRA entraînée sur 116 759 séquences de drone dans Unreal Engine)
- Wan 2.1 + lightx2v
- Workflows TEXT2SPHERE / IMG2SPHERE
- Export COLMAP → Brush (entraînement de splats) → SuperSplat (aperçu dans le navigateur)

**🗺️ La quête**

- [ ] Workflow panorama 360° : **TEXT2SPHERE** (depuis un prompt) ou **IMG2SPHERE** (depuis une image).
- [ ] Workflow dataset : dessiner la trajectoire du drone dans l'éditeur de chemin.
- [ ] Laisser Wan 2.1 combler les zones manquantes ; le panorama 8K d'origine est reprojeté par-dessus.
- [ ] Récupérer le dataset **COLMAP** et entraîner le Gaussian Splat dans **Brush**.
- [ ] Prévisualiser le splat dans le navigateur avec superspl.at/editor.

**💡 Astuces de Mick**

- Tous les modèles sont listés dans les notes à gauche du workflow. Installe le patch SageAttention pour accélérer.
- Approches testées puis écartées : Apple SHARP (préserve une scène au lieu d'en inventer), HY-World 2.0 et Lyra (trop lourds pour du matériel grand public), LoRA H3 (prometteur, pas encore au point).

> 🪤 **Piège :** N'upscale pas la sortie 720p : la reprojection du panorama 8K est plus nette et plus rapide (mieux que SeedVR2, selon Mick).

- [ ] ⚔️ **Défi (+300 XP) :** Générer un monde à partir d'une photo de vacances et y faire un vol de drone de 10 s.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/new-video-free-167647096)</sub>

<a id="l12"></a>

### Niveau 12 · 🕶️ Bullet time maison

**Difficulté** ★★★★★ · **Défi** +300 XP · **Badge** 🕶️ L'Élu·e

🎥 [4D video is here, and it runs on your own computer](https://www.youtube.com/watch?v=nYisoS5wPYQ) · 11 sept. 2026 · 8:21 · ~59 k vues

🖥️ GPU conseillé : **L40S (48 Go)** · pas de template RunPod de Mick (template ComfyUI générique)

Une vidéo de téléphone → **un humain en 4D** (un Gaussian Splat par frame) autour duquel une caméra virtuelle peut tourner. C'est le portage ComfyUI de **4DAnyone** (Ant Research et Université du Zhejiang), qui tourne sur Wan 2.2. Le test ultime : le *bullet time* de Matrix, fait à l'origine avec environ 120 caméras, refait par Mick avec un iPhone, un matelas et une demi-heure à embêter ses voisins.

**🎒 Ton arsenal**

- 4DAnyone sur Wan 2.2 (les LoRA d'accélération divisent les steps par plus de deux)
- SAM 3D Body (Meta, intégré à ComfyUI) à la place de GVHMR/SMPL-X
- gsplat via `install_splatkit.bat`
- Nerfstudio (optionnel), passe de nettoyage MiniMax H3

**🗺️ La quête**

- [ ] Installer les nœuds requis, puis déposer `install_splatkit.bat` à la racine de ComfyUI (à côté de `run_nvidia_gpu.bat`) et l'exécuter. Redémarrer.
- [ ] Charger une vidéo de **121 frames** exactement.
- [ ] Génération : régler `camera_preset` pour choisir le nombre de vues artificielles.
- [ ] **4DAnyone Export Frameset** : produit un dataset prêt pour l'entraînement (découpes, caméras, forme 3D grossière par frame).
- [ ] **Train Sequence** : un splat par frame, soit une séquence animée. Inspecter le résultat.

**💡 Astuces de Mick**

- SAM 3D Body remplace GVHMR, bâti sur SMPL-X et donc interdit en usage commercial.
- Tu peux aussi exporter le dataset et l'entraîner dans Nerfstudio.
- Le workflow a été mis à jour le 15/09/2026 (téléchargement automatique des modèles).

> 🪤 **Piège :** Le lecteur de séquence peut afficher 1 ou 2 frames cassées alors que les splats sont bons : vérifie dans le turnaround avant de paniquer.

- [ ] ⚔️ **Défi (+300 XP) :** Filmer un ami qui saute (121 frames), puis faire tourner la caméra virtuelle autour de lui en plein vol.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/new-video-free-169253362)</sub>

---

## Boss final

<a id="l13"></a>

### Niveau 13 · 👑 SHIBUWAKA : un anime avec un seul modèle

**Difficulté** ★★★★★ · **Défi** +400 XP · **Badge** 👑 Réalisateur·rice

🎥 [This NEW METHOD just solved AI FILMMAKING [Free & Local!]](https://www.youtube.com/watch?v=xwTJ3E3NU7A) · 28 oct. 2026 · 12:39 · ~84 k vues

🖥️ GPU conseillé : **RTX PRO 6000 (96 Go) ou H100** · pas de template RunPod de Mick (template ComfyUI générique)

La suite complète de workflows du court-métrage anime **SHIBUWAKA** : images, voix, musique et vidéo, le tout avec **MiniMax H3** (version référence). Chaque étape de production a son workflow, mais tous partagent la même disposition : les références entrent d'un côté (texte, image, audio ou vidéo), le prompt dit à quoi sert chacune, le résultat sort de l'autre côté.

**🎒 Ton arsenal**

- MiniMax H3 `ref2va` int8 (19,5 Gio)
- Qwen3-VL 32B : NVFP4 (RTX 50) ou int8 (cartes plus anciennes)
- VAE vidéo + VAE audio, Turbo LoRA v4
- RealESRGAN ×2, LoRA orbite 360°, ControlNet Union 2.0

**🗺️ La quête**

- [ ] **Character Sheets** : depuis un prompt, un croquis, un layout 3D ou des photos (l'équipe s'en est aussi servie pour les voitures).
- [ ] **Turnarounds** : une vidéo de rotation à partir d'une image ou d'une fiche.
- [ ] **Édition d'images et de décors** : lieux cohérents, images de début, de fin et de style.
- [ ] **Voix** : créer une réplique une fois, puis la réutiliser comme référence pour garder la même voix.
- [ ] **Vidéo** : suivre une réplique existante pour un timing exact, ou utiliser la voix comme référence pour de nouvelles phrases (avec un groupe pour références vidéo, flèches dessinées, storyboards et layouts Blender).
- [ ] **Upscaling** : re-rendre chaque plan en plus haute résolution avec le même prompt et les mêmes références. C'est ainsi que tout le film est passé en 4K.

**💡 Astuces de Mick**

- Une fois un workflow compris, les autres semblent familiers : même disposition partout.
- Le modèle 4 bits `w4a8` (11 Gio) est possible, mais environ 2× plus lent que l'int8 dans les tests de l'équipe.
- Les fichiers d'exemple du film sont réservés aux soutiens Patreon « Advanced » ; les workflows sont gratuits.

> 🪤 **Piège :** Le text encoder **NVFP4** est fait pour les cartes Blackwell (RTX 50, RTX PRO). Sur une carte plus ancienne, prends la version int8 (25,3 Gio).

- [ ] ⚔️ **Défi (+400 XP) :** BOSS : un court anime de 30 s avec 1 personnage (fiche + turnaround), 1 décor, 3 plans, des voix cohérentes, upscalé en 4K.

<sub>Source : [guide officiel sur mickmumpitz.ai](https://mickmumpitz.ai/posts/make-your-own-ai-173637693)</sub>

---

## 🌦️ La météo des modèles

Ce que ces 13 vidéos racontent de l'évolution de l'IA vidéo locale en un an.

| Quand | Quoi | Pour quoi faire |
|---|---|---|
| Nov.–déc. 2025 | **Qwen Image Edit + Wan 2.2** | Films plan par plan, upscaler génératif, marionnettes au bâton. |
| Févr.–mars 2026 | **Merge SkyReels V3 × Wan VACE** | AI Renderer 2.0 puis pipeline VFX complet : masques + ControlNets + références. |
| Mai–juin 2026 | **LTX 2.3 + Flux 2 Klein** | L'audio et le lip sync arrivent : Movie Builder modulaire, *Bear Minimum*. |
| Juil. 2026 | **Flux 2 Klein pour les datasets** | Character Creator 4.0 et entraînement de LoRA de casting. |
| Août–oct. 2026 | **MiniMax H3 partout** | Rendu clay → plan fini avec son, performance capture, nettoyage 4D, anime complet. Un seul modèle pour image, voix, musique et vidéo remplace les piles de ControlNet. |
| En parallèle | **Gaussian Splats** | Mondes explorables (Matrix-3D) et humains en 4D (4DAnyone). |

## 🧠 Le quiz du ciné-club

**Q1. Ta vidéo clay pour MiniMax H3 fait 150 frames. Combien seront rendues ?**

- A) 150
- B) 141
- C) 158
- D) 124

<details><summary>Réponse</summary>

**B) 141** : H3 travaille par paliers (90, 107, 124, 141, 158…) et coupe vers le bas : 150 → 141. Coupe ta passe à l'un de ces nombres.

</details>

**Q2. Quelle valeur de frames_per_iteration est valide pour Wan ?**

- A) 80
- B) 81
- C) 82
- D) 84

<details><summary>Réponse</summary>

**B) 81** : Wan suit la formule 4n+1 : 81 = 4×20+1.

</details>

**Q3. Tu veux que H3 garde un plan continu, sans coupe. Que mets-tu dans le prompt ?**

- A) « no camera move, no cut »
- B) « one continuous take »
- C) Rien, c'est automatique
- D) « static, static, static »

<details><summary>Réponse</summary>

**B) « one continuous take »** : Règle d'or H3 : dis ce que tu veux, pas ce que tu ne veux pas. Les négations ne font rien.

</details>

**Q4. Performance capture : tu portes un casque avec une caméra. Où dois-tu le mentionner ?**

- A) Dans le prompt
- B) Dans WHAT GETS REPLACED
- C) Nulle part
- D) Dans le prompt et dans WHAT GETS REPLACED

<details><summary>Réponse</summary>

**B) Dans WHAT GETS REPLACED** : SAM doit le masquer, donc il va dans WHAT GETS REPLACED. Mais le nommer dans le prompt pousse H3 à le dessiner.

</details>

**Q5. LTX 2.3 : l'image est parfaite mais la voix est fausse. Que changes-tu d'abord ?**

- A) La seed de l'étape 1
- B) La seed de l'étape 2
- C) Le prompt
- D) La référence voix

<details><summary>Réponse</summary>

**B) La seed de l'étape 2** : Le sampler a deux étapes : changer la seed de l'étape 2 corrige souvent la voix sans perdre le visuel.

</details>

**Q6. Quelle résolution est valide pour le rendu LTX du pipeline Bear Minimum ?**

- A) 1400×760
- B) 1408×768
- C) 1920×1080
- D) 1366×768

<details><summary>Réponse</summary>

**B) 1408×768** : Elle doit être divisible par 64 : 1408 = 22×64 et 768 = 12×64. (1080 ne l'est pas, d'où le 1920×1088.)

</details>

**Q7. Pourquoi Mick a-t-il remplacé GVHMR dans son portage de 4DAnyone ?**

- A) Trop lent
- B) Il repose sur SMPL-X, interdit en usage commercial
- C) Incompatible avec Wan 2.2
- D) Il ne gère pas les mains

<details><summary>Réponse</summary>

**B) Il repose sur SMPL-X, interdit en usage commercial** : SAM 3D Body (Meta, intégré à ComfyUI) fournit le même type de squelette sous une licence utilisable.

</details>

**Q8. Tu as entraîné une LoRA sur Flux 2 Klein 9B. Peux-tu vendre ce que tu produis avec ?**

- A) Oui, le fichier est à moi
- B) Non : la LoRA hérite de la licence non commerciale du modèle
- C) Oui, si je la renomme
- D) Seulement en dehors de l'UE

<details><summary>Réponse</summary>

**B) Non : la LoRA hérite de la licence non commerciale du modèle** : Une LoRA hérite de la licence de son modèle de base. Pour du commercial : CCC 3.5, la version Qwen, ou bientôt Krea 2.

</details>

**Q9. Bear Minimum : ton MP3 de dialogue est plus court que le plan. Que se passe-t-il ?**

- A) Du silence à la fin
- B) Le modèle invente de l'audio après la voix
- C) Une erreur bloque le rendu
- D) La voix tourne en boucle

<details><summary>Réponse</summary>

**B) Le modèle invente de l'audio après la voix** : L'audio doit durer au moins autant que le plan, et contenir le dialogue seul (pas de musique).

</details>

**Q10. Upscaler : que fait une valeur de Creativity élevée ?**

- A) Plus fidèle à la source
- B) Plus de détails inventés, avec un risque de bruit
- C) Plus rapide
- D) Change la résolution

<details><summary>Réponse</summary>

**B) Plus de détails inventés, avec un risque de bruit** : Creativity modifie le shift du scheduler : bas = fidèle, haut = génératif (avec des artefacts possibles).

</details>

**Q11. World Generation : comment obtenir un résultat net à partir de la sortie 720p ?**

- A) SeedVR2 sur la sortie 720p
- B) Reprojeter le panorama 8K d'origine
- C) Doubler les steps
- D) Rendre directement en 8K

<details><summary>Réponse</summary>

**B) Reprojeter le panorama 8K d'origine** : Reprojeter le panorama 8K est plus net et plus rapide que SeedVR2, selon l'équipe.

</details>

**Q12. Combien de frames doit faire ta vidéo pour le workflow 4D ?**

- A) 81
- B) 121
- C) 141
- D) 362

<details><summary>Réponse</summary>

**B) 121** : Le workflow 4DAnyone attend exactement 121 frames.

</details>

## 📖 Glossaire express

- **ComfyUI** : Interface en nœuds pour faire tourner des modèles d'IA en local. Tout le travail de Mick s'y passe.
- **Workflow (.json)** : Un graphe de nœuds sauvegardé. On le glisse sur le canvas pour le charger.
- **Custom nodes** : Extensions de ComfyUI. Le Manager installe celles qui manquent.
- **LoRA** : Petit fichier qui spécialise un modèle (un style, un personnage, plus de vitesse…).
- **VAE** : Encode et décode les images (ou le son) vers l'espace latent. H3 en utilise deux : image et son.
- **Text encoder** : Modèle qui comprend ton prompt (Gemma, Qwen3-VL…). Souvent aussi lourd que le modèle principal.
- **ControlNet** : Guide la génération avec une structure : depth, canny, pose, tracking.
- **Seed** : Le nombre qui fixe le hasard. Même seed + mêmes réglages = même résultat.
- **Steps** : Nombre d'étapes de débruitage. Les LoRA turbo ou lightning le réduisent à 4 ou 8.
- **fp8 / int8 / NVFP4 / GGUF** : Formats compressés des modèles : moins de VRAM, parfois un peu moins de qualité. NVFP4 est réservé aux RTX 50.
- **VACE** : Le module de Wan pour l'inpainting et le guidage par ControlNet.
- **Masque** : Image noir et blanc qui dit quelle zone garder ou régénérer. La convention change selon le workflow.
- **Clay / playblast** : Rendu 3D gris, sans texture, qui sert uniquement de mise en scène.
- **Look plate** : Image fixe qui donne la matière, la couleur et la lumière d'un rendu H3.
- **Gaussian Splat** : Représentation 3D en nuage de « taches » colorées, entraînée à partir de photos ou de vues générées.
- **COLMAP** : Format standard de dataset de photos + caméras pour entraîner des splats.
- **Équirectangulaire / 360°** : Image panoramique complète, dépliée en rectangle 2:1.
- **Lip sync** : Synchronisation des lèvres d'un personnage sur un fichier audio.
- **RunPod** : Location de GPU dans le cloud. Mick fournit un template prêt à l'emploi pour presque chaque workflow.
- **SageAttention** : Optimisation qui accélère Wan jusqu'à 2×. Mick fournit un installeur pour Windows.

## 🏆 Les rangs

Total possible : **4245 XP** (87 étapes × 10, les défis, et 12 questions × 25).

- 🎟️ **Stagiaire du plateau** : à partir de 0 % de l'XP
- 🎥 **Assistant·e caméra** : à partir de 10 % de l'XP
- 💡 **Chef·fe opérateur·rice** : à partir de 30 % de l'XP
- ✨ **Superviseur·e VFX** : à partir de 55 % de l'XP
- 🎬 **Réalisateur·rice** : à partir de 80 % de l'XP
- 🏆 **Mickmumpitz d'honneur** : à partir de 100 % de l'XP

## 📚 Sources

Les 13 dernières vidéos de la [chaîne YouTube @mickmumpitz](https://www.youtube.com/@mickmumpitz) (liste relevée le 11 novembre 2026). Le contenu vient des **posts compagnons publics** que Mickmumpitz publie avec chaque vidéo sur [mickmumpitz.ai](https://mickmumpitz.ai), reformulés et organisés en progression. Les transcriptions des vidéos n'ont pas pu être récupérées : regarde les vidéos pour les démonstrations détaillées.

| Date | Vidéo | Durée | Niveau | Guide |
|---|---|---|---|---|
| 28 oct. 2026 | [This NEW METHOD just solved AI FILMMAKING [Free & Local!]](https://www.youtube.com/watch?v=xwTJ3E3NU7A) | 12:39 | [Niv. 13](#l13) | [guide](https://mickmumpitz.ai/posts/make-your-own-ai-173637693) |
| 7 oct. 2026 | [I turned myself into a pig with AI [Free & Local VFX Tutorial]](https://www.youtube.com/watch?v=2MzCkYrvDaI) | 11:30 | [Niv. 10](#l10) | [guide](https://mickmumpitz.ai/posts/new-video-free-171702311) |
| 11 sept. 2026 | [4D video is here, and it runs on your own computer](https://www.youtube.com/watch?v=nYisoS5wPYQ) | 8:21 | [Niv. 12](#l12) | [guide](https://mickmumpitz.ai/posts/new-video-free-169253362) |
| 25 août 2026 | [We Open Sourced World Generation](https://www.youtube.com/watch?v=eJuYBNrD8HI) | 17:55 | [Niv. 11](#l11) | [guide](https://mickmumpitz.ai/posts/new-video-free-167647096) |
| 12 août 2026 | [This new AI Render Engine is amazing!](https://www.youtube.com/watch?v=Q2oVBVQNQ_U) | 14:09 | [Niv. 8](#l8) | [guide](https://mickmumpitz.ai/posts/new-video-free-166463119) |
| 17 juil. 2026 | [Create HYPERREALISTIC AI Characters That INTERACT | FREE & LOCAL](https://www.youtube.com/watch?v=ghFYDG0DF1w) | 34:28 | [Niv. 2](#l2) | [guide](https://mickmumpitz.ai/posts/new-video-free-4-164075060) |
| 19 juin 2026 | [We Built a FREE AI Render Engine for CG & Facial Animation (ComfyUI + Blender)](https://www.youtube.com/watch?v=7J7hi-Hxpac) | 23:00 | [Niv. 7](#l7) | [guide](https://mickmumpitz.ai/posts/new-video-free-161509029) |
| 4 mai 2026 | [Generate ENTIRE AI MOVIES with this NEW METHOD! [FREE & LOCAL]](https://www.youtube.com/watch?v=0mT4p86ZxGQ) | 10:43 | [Niv. 4](#l4) | [guide](https://mickmumpitz.ai/posts/guide-ltx-2-3-157329723) |
| 30 mars 2026 | [I Built a FREE VFX Pipeline with Local AI [2026 Masterclass]](https://www.youtube.com/watch?v=_n0ir5V5tX4) | 19:12 | [Niv. 9](#l9) | [guide](https://mickmumpitz.ai/posts/free-guide-epic-154092216) |
| 25 févr. 2026 | [We Built an AI Render Engine for FREE](https://www.youtube.com/watch?v=0WkixvqnPXw) | 15:30 | [Niv. 6](#l6) | [guide](https://mickmumpitz.ai/posts/ai-renderer-2-0-153434545) |
| 17 déc. 2025 | [AI is finally controllable! …with a stick [FREE ComfyUI Workflow + Tutorial]](https://www.youtube.com/watch?v=pUb58eAZ3pc) | 10:40 | [Niv. 5](#l5) | [guide](https://mickmumpitz.ai/posts/ai-video-with-154392669) |
| 25 nov. 2025 | [STOP Paying for AI Upscaling - This FREE Tool is BETTER [ComfyUI Workflow + Tutorial]](https://www.youtube.com/watch?v=NpNagmQI4yg) | 9:16 | [Niv. 1](#l1) | [guide](https://mickmumpitz.ai/posts/stop-paying-for-154393504) |
| 1 nov. 2025 | [Build your own AI MOVIES shot by shot - FREE & LOCAL! [AI Filmmaking in ComfyUI - 2025]](https://www.youtube.com/watch?v=YpuSE9hcal8) | 17:00 | [Niv. 3](#l3) | [guide](https://mickmumpitz.ai/posts/new-video-build-142571395) |

<sub>Guide non officiel, fait par un fan. Tous les workflows, vidéos et noms appartiennent à Mickmumpitz et à leurs auteurs respectifs.</sub>
