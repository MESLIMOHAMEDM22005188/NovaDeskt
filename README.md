# NovaDeskt

**Application desktop de gestion de projets et de tâches — Java · JavaFX · SQLite**

NovaDeskt est une application desktop conçue pour centraliser la gestion de projets, organiser les tâches et suivre leur avancement dans un espace de travail unifié.

Le projet vise à mettre en œuvre des pratiques de développement logiciel rigoureuses : séparation des responsabilités, architecture modulaire, persistance relationnelle, tests automatisés et intégration continue.

> **Statut :** En cours de développement.

---

## Sommaire

- [Présentation](#présentation)
- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Architecture logicielle](#architecture-logicielle)
- [Modèle de données](#modèle-de-données)
- [Prérequis](#prérequis)
- [Installation et exécution](#installation-et-exécution)
- [Tests](#tests)
- [Intégration continue](#intégration-continue)
- [Conventions de développement](#conventions-de-développement)
- [Feuille de route](#feuille-de-route)
- [Structure du projet](#structure-du-projet)
- [Licence](#licence)

## Présentation

Les outils de gestion de projets doivent permettre de structurer le travail, de visualiser les priorités et de conserver un historique exploitable des informations.

NovaDeskt explore cette problématique à travers une application locale reposant sur une architecture Java modulaire. L'objectif est de construire progressivement un outil maintenable, testable et évolutif, tout en approfondissant les concepts de conception logicielle et de persistance des données.

L'application est développée pour fonctionner sur un poste de travail, sans dépendre d'un serveur applicatif distant pour le stockage principal.

## Fonctionnalités

### Socle initial

- Structure applicative organisée en couches.
- Modèles métier `Project` et `Task`.
- Initialisation automatique d'une base SQLite.
- Schéma relationnel avec contraintes d'intégrité.
- Accès aux données par JDBC et requêtes préparées.
- Services dédiés aux premières validations métier.
- Interface graphique JavaFX avec chargement de vues FXML.
- Feuille de style CSS pour l'interface.
- Premier test unitaire du modèle métier.
- Workflow GitHub Actions pour l'exécution des tests.

### Évolutions prévues

- Création, modification et suppression des projets et des tâches.
- Tableau de bord avec indicateurs d'avancement.
- Tableau Kanban avec les statuts « À faire », « En cours » et « Terminé ».
- Gestion des échéances et des priorités.
- Recherche, filtres et tri des tâches.
- Export de rapports.
- Renforcement de la couverture de tests.
- Gestion centralisée des erreurs et journalisation.
- Amélioration de l'accessibilité et de l'expérience utilisateur.

Les éléments de cette seconde liste ne sont pas considérés comme implémentés tant que leur développement et leur validation ne sont pas terminés.

## Stack technique

| Technologie | Rôle |
|---|---|
| Java 21 LTS | Langage et plateforme d'exécution |
| JavaFX | Interface graphique desktop |
| FXML | Définition déclarative des vues |
| CSS | Présentation et styles de l'interface |
| Maven | Gestion des dépendances et cycle de build |
| SQLite | Persistance relationnelle locale |
| JDBC | Accès à la base de données |
| JUnit 5 | Tests unitaires |
| Git et GitHub | Versionnement et hébergement du code |
| GitHub Actions | Intégration continue |

## Architecture logicielle

NovaDeskt adopte une architecture en couches inspirée du modèle MVT, adaptée à une application desktop JavaFX.

```text
Utilisateur
    |
    v
View / Templates (FXML, CSS)
    |
    v
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
SQLite
```

### Responsabilités des couches

| Couche | Responsabilité |
|---|---|
| `model` | Représentation des entités et invariants métier |
| `view` / FXML | Structure déclarative des écrans |
| `controller` | Réception des interactions et coordination de l'interface |
| `service` | Validations et règles métier |
| `repository` | Lecture et écriture des données |
| `config` | Configuration et initialisation de la base |

Cette séparation vise à limiter le couplage entre l'interface, les règles métier et le stockage. Elle facilite les tests et permet de faire évoluer chaque composant indépendamment.

**Précision architecturale :** il s'agit d'une adaptation du MVT, et non d'une implémentation stricte du modèle historique utilisé par certains frameworks web. Dans JavaFX, les contrôleurs FXML jouent le rôle de médiateurs entre les vues et les services.

## Modèle de données

La base SQLite contient initialement deux entités principales.

### `projects`

| Champ | Description |
|---|---|
| `id` | Identifiant unique |
| `name` | Nom du projet |
| `description` | Description du projet |
| `created_at` | Date de création |

### `tasks`

| Champ | Description |
|---|---|
| `id` | Identifiant unique |
| `project_id` | Référence vers le projet associé |
| `title` | Titre de la tâche |
| `description` | Description détaillée |
| `status` | État d'avancement |
| `due_date` | Date d'échéance facultative |
| `created_at` | Date de création |

La relation entre `tasks` et `projects` est de type plusieurs-vers-un : un projet peut regrouper plusieurs tâches. Une clé étrangère assure l'intégrité référentielle.

La base est créée au premier lancement dans `data/novadeskt.db`. Ce fichier est destiné à rester local et n'a pas vocation à être versionné dans Git.

## Prérequis

- JDK 21.
- IntelliJ IDEA ou un environnement Java compatible.
- Git.
- Une connexion Internet lors du premier téléchargement des dépendances Maven.

Maven est déclaré dans le fichier `pom.xml`. Le Maven Wrapper sera ajouté pour garantir une version de Maven reproductible entre les environnements.

## Installation et exécution

### 1. Cloner le dépôt

```bash
git clone <URL_DU_DEPOT>
cd NovaDeskt
```

Remplace `<URL_DU_DEPOT>` par l'URL réelle du dépôt GitHub.

### 2. Configurer Java

Dans IntelliJ IDEA, ouvrir :

`File → Project Structure → Project`

Sélectionner le JDK 21 comme SDK du projet et vérifier le niveau de langage Java.

### 3. Charger le projet Maven

Ouvrir `pom.xml`, puis importer ou recharger le projet Maven afin de télécharger les dépendances déclarées.

### 4. Lancer l'application

Après configuration du Maven Wrapper, depuis la racine du projet :

Sous Windows :

```powershell
.\mvnw.cmd javafx:run
```

Sous Linux ou macOS :

```bash
./mvnw javafx:run
```

La commande ne pourra être utilisée qu'après la génération et l'ajout du Maven Wrapper au dépôt.

### 5. Initialisation de la base

Au démarrage, l'application initialise le schéma SQLite à partir de `src/main/resources/db/schema.sql`.

Le fichier de base est créé dans `data/novadeskt.db` si nécessaire.

## Tests

Les tests automatisés reposent sur JUnit 5.

Après configuration du Maven Wrapper :

Sous Windows :

```powershell
.\mvnw.cmd test
```

Sous Linux ou macOS :

```bash
./mvnw test
```

Les tests doivent couvrir progressivement les invariants des modèles, les règles métier des services et les opérations de persistance des repositories.

La couverture de tests et les résultats d'exécution devront être évalués à mesure que le projet évolue.

## Intégration continue

Le workflow GitHub Actions défini dans `.github/workflows/ci.yml` prévoit l'exécution des tests sur un environnement Linux avec Java 21.

Il est déclenché lors des push et des pull requests.

Le Maven Wrapper devra être ajouté pour permettre au workflow d'utiliser la même version de Maven que les développeurs locaux.

## Conventions de développement

Les principes suivants guident le développement :

- **Responsabilité unique :** chaque classe doit avoir un rôle clairement défini.
- **Encapsulation :** les invariants métier sont protégés par les modèles et les services.
- **Faible couplage :** l'interface ne doit pas porter les règles métier ni les requêtes SQL.
- **Persistance sécurisée :** les requêtes SQL utilisent des paramètres préparés.
- **Gestion explicite des erreurs :** les erreurs techniques et métier doivent être traitées à des niveaux adaptés.
- **Tests automatisés :** chaque nouvelle fonctionnalité importante doit être accompagnée de tests.
- **Versionnement lisible :** les commits doivent décrire précisément les changements réalisés.

## Feuille de route

- [x] Initialisation du projet Maven.
- [x] Définition du schéma SQLite initial.
- [x] Création des premiers modèles métier.
- [x] Mise en place des premières couches repository et service.
- [x] Première vue JavaFX et son contrôleur.
- [x] Configuration initiale de GitHub Actions.
- [ ] Génération du Maven Wrapper.
- [ ] Validation de la compilation et de l'exécution.
- [ ] Finalisation des opérations CRUD.
- [ ] Développement du tableau Kanban.
- [ ] Ajout du tableau de bord et des statistiques.
- [ ] Renforcement des tests unitaires et d'intégration.
- [ ] Documentation technique et captures d'écran.
- [ ] Préparation d'une première version stable.

## Structure du projet

```text
NovaDeskt/
├── .github/
│   └── workflows/
│       └── ci.yml
├── data/
├── docs/
│   └── ARCHITECTURE.md
├── src/
│   ├── main/
│   │   ├── java/fr/novadeskt/
│   │   │   ├── App.java
│   │   │   ├── config/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── controller/
│   │   └── resources/
│   │       ├── css/
│   │       ├── db/
│   │       └── fxml/
│   └── test/
│       └── java/fr/novadeskt/
├── .gitignore
├── pom.xml
└── README.md
```

## Licence

Aucune licence n'est encore définie. Les conditions de réutilisation et de distribution seront précisées lors de la publication d'une version dédiée.

