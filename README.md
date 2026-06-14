# GeoTrouvetou — Application de partage d'événements géolocalisés

Application communautaire permettant de créer, découvrir et rejoindre des événements
géolocalisés en temps réel sur une carte interactive.

---

## Équipe

- Noah Faisca
- Sara Terrier
- Lucas Gonthier
- Maxime Boulenger

**Maquette Figma :** https://www.figma.com/design/GlBFQ5gmfbQNfVI2q0KQYG/Geo-Trouvetou?node-id=1-6

---

## Niveau réalisé

**Option 2 — Niveau Intermédiaire**, intégralement couverte (authentification
email/mot de passe, rôles utilisateur/administrateur, upload d'image, filtrage par
distance, temps réel, BroadcastReceiver).

Au-delà du sujet, le projet ajoute les fonctionnalités suivantes :

| Fonctionnalité supplémentaire               | Description                                                   |
|---------------------------------------------|---------------------------------------------------------------|
| Jetpack Compose                             | UI sans XML ni fragments                                      |
| Bibliothèque de composants maison           | `atoms` / `molecules` / `organisms` calqués sur la maquette Figma |
| Maquette Figma complète                     | conception préalable de toute l'interface                     |
| Événements privés / brouillons              | un événement privé n'est visible que par son créateur         |
| Inscription / Désinscription d'un événement | l'utilisateur peut rejoindre ou quitter un événement       |
| Clustering de marqueurs                     | regroupement automatique des marqueurs proches sur la carte   |
| Recherche d'adresse géocodée                | autocomplétion d'adresses via Nominatim (OSM)                 |
| Journal d'activité admin                    | historique daté des actions (table `audit_log`)               |
| Langues FR / EN                             | tous les textes externalisés, bascule selon la langue du téléphone |

---

## Guide d'utilisation

### La carte (écran d'accueil)
- Au lancement, l'app demande la **permission de localisation** puis centre la carte sur votre
  position. Les événements proches sont chargés automatiquement.
- Les **marqueurs** représentent les événements ; ceux trop proches sont **regroupés en cluster**
  (un cercle avec un nombre) — toucher un cluster ouvre la liste des événements regroupés.
- Un marqueur **vert** est un événement public ; un marqueur **orange** est un de vos événements
  **privés** (brouillons), visible de vous seul.
- Boutons flottants : **cible** (recentrer sur ma position), **+ / −** (zoom).
- Toucher un marqueur ouvre la **fiche de l'événement** (date, lieu, description) avec le bouton
  d'action correspondant (Rejoindre, Se désinscrire, ou Modifier/Supprimer si vous en êtes l'auteur).

### La barre de recherche (en bas de la carte)
- **Sans saisie** : elle ouvre la liste de **tous les événements actuellement visibles à l'écran**.
  Déplacer ou zoomer la carte met cette liste à jour (chargement par zone visible).
- **Avec une adresse** : l'autocomplétion (Nominatim/OSM) propose des lieux ; en sélectionner un
  **déplace la carte** sur ce lieu et affiche **tous les événements à proximité**.
- C'est le moyen rapide d'explorer une autre ville sans faire défiler la carte manuellement.

### Créer / modifier un événement
- Onglet central **+** : formulaire de création (titre, description, date, heure, lieu, image de
  couverture, visibilité public/privé). Le champ lieu utilise la même recherche d'adresse géocodée.
- En tant qu'auteur, la fiche d'un événement affiche **Modifier** et **Supprimer** (avec
  confirmation). Un événement **privé** sert de brouillon : il n'apparaît que pour vous tant que
  vous ne le passez pas en public.

### Profil & participations
- Onglet **profil** : vos statistiques (événements créés, participations).
- Onglet **Mes événements** : les événements que vous avez créés.
- Onglet **Mes participations** : les événements que vous avez **rejoints**, regroupés ici pour les
  **retrouver facilement** (toucher un élément rouvre sa fiche). Le bouton *Se désinscrire* d'une
  fiche les retire de cette liste.

### Administration (réservé au rôle admin)
Accessible via **Profil → Paramètres → Administration** (l'entrée n'apparaît que pour un compte
administrateur). Le panneau propose :
- **Aperçu** : statistiques globales (nombre d'utilisateurs, d'événements) et **journal d'activité**
  horodaté (créations de comptes/événements, changements de rôle, suppressions).
- **Utilisateurs** : liste paginée, **changement de rôle** (utilisateur ↔ administrateur) et
  **suppression de compte**.
- **Événements** : liste paginée de tous les événements avec **suppression** (modération).

---

## Architecture

Le projet suit une architecture **MVVM** (Model – View – ViewModel) avec une séparation stricte
des couches via des interfaces de domaine.

```
┌─────────────────────────────────────────────────────────┐
│  UI (Jetpack Compose)                                   │
│  Screens · Composants · ViewModels                      │
├─────────────────────────────────────────────────────────┤
│  Domain                                                 │
│  Interfaces (IAuthService, IDatabaseService, …)         │
│  Modèles métier (Evenement, User, Place, …)             │
├─────────────────────────────────────────────────────────┤
│  Data                                                   │
│  SupabaseAuthService · SupabaseDatabaseService          │
│  SupabaseImageService · NominatimGeocodingService       │
│  OSMMapService                                          │
├─────────────────────────────────────────────────────────┤
│  App.kt — ServiceLocator                                │
│  Instancie les implémentations et les expose            │
│  derrière leurs interfaces                              │
└─────────────────────────────────────────────────────────┘
```

### Modularité

`App.kt` est le seul point de câblage. Remplacer Supabase ou OpenStreetMap se résume à :
1. Écrire une nouvelle classe implémentant l'interface concernée.
2. Modifier les 1 à 2 lignes d'instanciation dans `App.kt`.

Aucun ViewModel ni écran n'importe de type Supabase ou OSM directement.

```kotlin
// App.kt — changer de backend = changer cette ligne
val authService: IAuthService by lazy { SupabaseAuthService(supabase) }
// → MonAutreAuthService(...)
```

---

## Choix techniques

### Langage & UI
- **Kotlin** : langage unique, coroutines pour toutes les opérations asynchrones.
- **Jetpack Compose** : UI déclarative ; pas de XML, pas de fragments.
- Bibliothèque de composants maison (`atoms` / `molecules` / `organisms`) calquée sur la
  maquette Figma, garantissant cohérence visuelle et réutilisabilité.

### Cartographie
- **osmdroid 6.1.20** + **osmbonuspack 6.9.0** : carte OpenStreetMap, clustering de marqueurs
  par rayon (`RadiusMarkerClusterer`).
- **Nominatim** (API OSM) : autocomplétion d'adresses lors de la création / modification d'un
  événement, sans clé API.
- Le filtrage par zone visible (`MapBounds`) limite les requêtes Supabase à la portion de carte
  affichée, évitant de charger l'ensemble des événements.

### Backend
- **Supabase 3.6.0** via le SDK Kotlin officiel (`supabase-kt`).
  - `postgrest-kt` : CRUD événements, profils, participants.
  - `auth-kt` : authentification email / mot de passe, gestion des sessions.
  - `storage-kt` : upload des images de couverture et des avatars (format WebP).
  - `realtime-kt` : écoute des changements en base pour rafraîchir la carte automatiquement.
- Les credentials sont injectés au build depuis `local.properties` via `BuildConfig`, jamais
  codés en dur dans les sources.

### Visibilité des événements
- Chaque événement porte un booléen `visibility`. Un événement **privé** (brouillon) n'est
  retourné par les requêtes qu'à son créateur ; les événements publics sont visibles de tous.
- Le filtrage est appliqué côté requête (`visibleToCurrentUser`) : `visibility = true` **ou**
  `user_id = utilisateur courant`.

### Permissions & récepteurs système
- Permissions de localisation demandées au runtime (`ACCESS_FINE_LOCATION`,
  `ACCESS_COARSE_LOCATION`).
- `NetworkReceiver` : utilise `ConnectivityManager.NetworkCallback` (API moderne) pour détecter
  perte et retour de la connexion ; affiche un toast contextuel.
- `LocationReceiver` : `BroadcastReceiver` sur `LocationManager.PROVIDERS_CHANGED_ACTION` pour
  détecter l'activation / désactivation du GPS.

### Sécurité
- La clé Supabase et l'URL sont stockées dans `local.properties` et injectées via `BuildConfig`
  plutôt que codées en dur dans les sources.
- Les règles de sécurité Supabase (Row Level Security) garantissent qu'un utilisateur ne peut
  modifier que ses propres données.
- Les mots de passe ne transitent jamais en clair dans le code.

### Administration
- Les utilisateurs avec le rôle `admin` accèdent à un panneau dédié :
  statistiques globales, gestion des comptes et des événements, logs d'activité datés.
- Les actions d'administration sont tracées dans une table `audit_log` côté Supabase.

---

## Organisation des packages

```
fr.miage.geotrouvetou/
│
├── App.kt                          # ServiceLocator — point d'entrée
│
├── data/
│   ├── backend/
│   │   ├── SupabaseAuthService     # IAuthService    → Supabase Auth
│   │   ├── SupabaseDatabaseService # IDatabaseService → Postgrest + Realtime
│   │   └── SupabaseImageService    # IImageService   → Supabase Storage
│   ├── geocoding/
│   │   └── NominatimGeocodingService # IGeocodingService → API Nominatim
│   └── maps/
│       ├── OSMMapService           # IMapService → osmdroid
│       └── MarkerIconRenderer      # Génération des icônes de marqueurs
│
├── domain/
│   ├── interfaces/                 # Contrats — couche indépendante du backend
│   │   ├── IAuthService
│   │   ├── IDatabaseService
│   │   ├── IGeocodingService
│   │   ├── IImageService
│   │   └── IMapService
│   └── models/                     # Modèles métier (aucune dépendance Supabase)
│       ├── Evenement
│       ├── EventParticipant
│       ├── User · AdminStats · AuditLogEntry
│       └── Place
│
├── receivers/
│   ├── NetworkReceiver             # Perte / retour réseau (NetworkCallback)
│   └── LocationReceiver            # Activation / désactivation GPS
│
├── ui/
│   ├── admin/                      # Panneau admin (aperçu, utilisateurs, événements)
│   ├── auth/                       # Connexion, inscription, traduction des erreurs
│   ├── components/
│   │   ├── atoms/                  # Button, Input, Switch, Checkbox, Toast, …
│   │   ├── molecules/              # EventCard, NavBar, PlaceSearchBar, …
│   │   └── organisms/              # EventDetailBody, Modal, SearchBar
│   ├── events/                     # Formulaire (création/édition) + détail d'un événement
│   ├── map/                        # MainActivity, écran carte + modals
│   ├── navigation/                 # NavGraph (Navigation Compose)
│   ├── params/                     # Paramètres utilisateur
│   ├── profile/                    # Profil, avatar, changement de mot de passe
│   └── utils/
│       ├── ViewModelFactories.kt   # Factory partagée alimentée par le ServiceLocator
│       ├── SystemStatusToasts.kt   # Surveillance réseau / GPS (toasts globaux)
│       └── EventDateFormatter.kt   # Formatage des dates et statuts d'événement
│
└── utils/
    ├── PasswordValidator           # Règles de validation du mot de passe
    └── UserFieldValidator          # Validation des champs nom / prénom / email
```

---

## Procédure d'installation

### Prérequis

- Android Studio
- JDK 21+
- Un émulateur ou un appareil en **API 36+**

### Étapes

**1. Décompresser l'archive**

Extraire le zip puis ouvrir le dossier obtenu dans Android Studio
(**File → Open**).

**2. Credentials Supabase**

Le fichier `local.properties` est déjà fourni à la racine avec l'URL et la clé `anon` du projet
Supabase :

```properties
SUPABASE_URL="https://<projet>.supabase.co"
SUPABASE_KEY="<clé-anon-publique>"
```

Aucune configuration supplémentaire n'est nécessaire.

**3. Synchroniser Gradle**

À l'ouverture, Android Studio lance le sync automatiquement. Au besoin :
**File → Sync Project with Gradle Files** (ou `./gradlew assembleDebug` en ligne de commande).

**4. Lancer l'application**

Sélectionner un émulateur (API 36+) ou un appareil physique, puis exécuter le projet.

> **Note :** la géolocalisation nécessite un appareil ou un émulateur avec le GPS activé.
> Il est possible de modifier la localisation par défaut dans les paramètres de l'émulateur.
> Sur l'émulateur Android Studio : Extended Controls → Location.

---

## Comptes de test

Deux comptes sont préprovisionnés pour la démonstration :

| Rôle          | Email                       | Mot de passe                |
|---------------|-----------------------------|-----------------------------|
| Administrateur | `admin@geotrouvetou.fr`     | `Admingeotrouvetou1@`       |
| Utilisateur   | `utilisateur@geotrouvetou.fr` | `Utilisateurgeotrouvetou1@` |

- Le **compte administrateur** donne accès au panneau d'administration
  (Profil → Paramètres → Administration).
- Le compte administrateur possède **un événement privé à Paris** : connecté avec ce compte, il
  apparaît sur la carte sous la forme d'un **marqueur orange** (les événements publics sont en
  vert). Ce même événement est invisible pour le compte utilisateur, ce qui illustre la
  visibilité des brouillons.