# Architecture du projet Abalone

## Vue d'ensemble

Le projet Abalone est une **API REST** construite avec **Spring Boot** et **Maven**.
Elle permet de jouer au jeu de plateau Abalone via des requetes HTTP (JSON) et propose
une interface web accessible sur `http://localhost:8080`.

L'architecture suit le pattern classique **Controller - Service - Repository** de Spring Boot,
avec une base de donnees **H2** en memoire pour la persistance.

---

## Pourquoi cette architecture ?

### Pattern Controller - Service - Repository

```
Client HTTP  ->  Controller  ->  Service  ->  Repository  ->  Base de donnees H2
(JSON)           (endpoints)     (logique)    (acces BDD)
```

- **Controller** : Recoit les requetes HTTP, valide le format, et renvoie les reponses JSON.
  Il ne contient **aucune logique metier** -- il delegue tout au Service.

- **Service** : Contient toute la **logique metier** du jeu (creer une partie, jouer un coup,
  verifier les regles, calculer les scores). C'est le cerveau de l'application.

- **Repository** : Interface vers la base de donnees. Spring Data JPA genere automatiquement
  les requetes SQL a partir des noms de methodes (ex: `findByGameId`).

### Pourquoi separer en couches ?

1. **Separation des responsabilites** : chaque couche a un role clair
2. **Testabilite** : on peut tester la logique metier sans demarrer le serveur HTTP
3. **Maintenabilite** : modifier les regles du jeu n'impacte pas les endpoints
4. **Reutilisabilite** : le Service peut etre utilise par un autre controller (WebSocket, CLI...)

---

## Structure des packages

```
com.abalone/
|-- AbaloneApplication.java          # Point d'entree Spring Boot
|-- controller/
|   |-- GameController.java          # CRUD parties + jouer
|   |-- PlayerController.java        # CRUD joueurs
|   |-- ScoreController.java         # Scores + Hall of Fame
|-- service/
|   |-- GameService.java             # Logique parties + cycle de vie
|   |-- PlayerService.java           # Logique joueurs
|   |-- BoardService.java            # Logique plateau + regles Abalone
|   |-- ScoreService.java            # Calcul + classement des scores
|-- repository/
|   |-- GameRepository.java
|   |-- PlayerRepository.java
|   |-- BoardCellRepository.java
|   |-- ScoreRepository.java
|-- model/
|   |-- Game.java                    # Entite JPA : une partie
|   |-- Player.java                  # Entite JPA : un joueur
|   |-- BoardCell.java               # Entite JPA : une case du plateau
|   |-- Score.java                   # Entite JPA : un score
|   |-- enums/
|       |-- GameStatus.java          # WAITING, IN_PROGRESS, FINISHED, ABANDONED
|       |-- CellState.java           # EMPTY, BLACK, WHITE
|       |-- Direction.java           # Les 6 directions hexagonales
|-- dto/                             # Objets de transfert (requetes/reponses JSON)
|-- exception/                       # Exceptions metier + handler global
```

---

## Les tables de la base de donnees et leur utilite

La base H2 contient **4 tables** qui modelisent l'etat complet du jeu.

### Table `player` -- Les joueurs

| Colonne      | Type      | Description                          |
|--------------|-----------|--------------------------------------|
| id           | BIGINT    | Identifiant unique auto-genere       |
| username     | VARCHAR   | Identifiant unique du joueur         |
| display_name | VARCHAR   | Nom d'affichage                      |
| created_at   | TIMESTAMP | Date de creation                     |

**Pourquoi cette table ?**
Les joueurs existent **independamment des parties**. Un joueur peut participer a plusieurs
parties. Le `username` unique permet d'identifier chaque joueur. Le CRUD complet permet
de creer, lire, modifier et supprimer des joueurs.

### Table `game` -- Les parties

| Colonne                 | Type      | Description                                      |
|-------------------------|-----------|--------------------------------------------------|
| id                      | BIGINT    | Identifiant unique auto-genere                   |
| status                  | VARCHAR   | IN_PROGRESS, FINISHED, ABANDONED                 |
| current_color           | VARCHAR   | Tour actuel : BLACK ou WHITE                     |
| black_out               | INT       | Billes noires ejectees                           |
| white_out               | INT       | Billes blanches ejectees                         |
| player_black_id         | BIGINT    | FK vers le joueur noir                           |
| player_white_id         | BIGINT    | FK vers le joueur blanc                          |
| winner_id               | BIGINT    | FK vers le joueur gagnant (null si en cours)     |
| turn_number             | INT       | Numero du tour actuel                            |
| created_at              | TIMESTAMP | Date de creation                                 |
| finished_at             | TIMESTAMP | Date de fin (null si en cours)                   |

**Pourquoi cette table ?**
Elle stocke l'etat global d'une partie : qui joue, quel est le score, la partie est-elle finie ?
Elle reference deux joueurs (FK) et optionnellement un gagnant.

**Cycle de vie d'une partie :**
```
Creation -> IN_PROGRESS -> FINISHED (victoire par 6 ejections)
                        -> ABANDONED (abandon volontaire)
```

**Quid de la suppression ?** Seules les parties terminees (FINISHED ou ABANDONED) peuvent
etre supprimees. Une partie en cours doit d'abord etre abandonnee.

### Table `board_cell` -- Le plateau (les cases)

| Colonne  | Type    | Description                                          |
|----------|---------|------------------------------------------------------|
| id       | BIGINT  | Identifiant unique auto-genere                       |
| game_id  | BIGINT  | FK vers la partie                                    |
| row_idx  | INT     | Coordonnee ligne (0 a 8)                             |
| col_idx  | INT     | Coordonnee colonne (variable par ligne)              |
| state    | VARCHAR | EMPTY, BLACK ou WHITE                                |

**Pourquoi cette table ?**
C'est la table la plus volumineuse : elle represente **chaque case du plateau hexagonal**.
Une partie d'Abalone a **61 cases** (lignes de 5,6,7,8,9,8,7,6,5).
Chaque case peut etre vide ou contenir une bille noire/blanche.
En persistant chaque case en BDD, on peut avoir plusieurs parties en parallele
et reprendre apres un redemarrage.

### Table `score` -- Les scores

| Colonne         | Type      | Description                                    |
|-----------------|-----------|------------------------------------------------|
| id              | BIGINT    | Identifiant unique auto-genere                 |
| player_id       | BIGINT    | FK vers le joueur                              |
| game_id         | BIGINT    | FK vers la partie                              |
| won             | BOOLEAN   | Le joueur a-t-il gagne ?                       |
| marbles_ejected | INT       | Nombre de billes adverses ejectees             |
| marbles_lost    | INT       | Nombre de billes propres perdues               |
| turns_played    | INT       | Nombre de tours de la partie                   |
| score_points    | INT       | Score calcule en points                        |
| created_at      | TIMESTAMP | Date de creation                               |

**Pourquoi cette table ?**
Elle enregistre le **resultat de chaque joueur a chaque partie**. Deux scores sont crees
par partie terminee (un par joueur). Cela permet de calculer le **Hall of Fame** (classement
general) et de consulter l'historique detaille de chaque joueur.

**Calcul du score :**
- Victoire : +100 points
- Bille adverse ejectee : +10 points chacune
- Bille propre perdue : -5 points chacune
- Bonus efficacite : +50 si victoire en moins de 30 tours
- Minimum : 0 points

### Relations entre les tables

```
player (1) ---- (N) game (via player_black_id, player_white_id, winner_id)
game   (1) ---- (N) board_cell
player (1) ---- (N) score
game   (1) ---- (2) score (un par joueur a la fin)
```

---

## Les endpoints de l'API

### Gestion des joueurs (CRUD)

| Methode | URL                  | Description              |
|---------|----------------------|--------------------------|
| GET     | `/api/players`       | Lister tous les joueurs  |
| GET     | `/api/players/{id}`  | Recuperer un joueur      |
| POST    | `/api/players`       | Creer un joueur          |
| PUT     | `/api/players/{id}`  | Modifier un joueur       |
| DELETE  | `/api/players/{id}`  | Supprimer un joueur      |

### Gestion des parties

| Methode | URL                            | Description                          |
|---------|--------------------------------|--------------------------------------|
| POST    | `/api/games`                   | Creer une nouvelle partie            |
| GET     | `/api/games`                   | Lister les parties (?status=filtre)  |
| GET     | `/api/games/{id}`              | Recuperer l'etat d'une partie        |
| DELETE  | `/api/games/{id}`              | Supprimer (si FINISHED/ABANDONED)    |
| POST    | `/api/games/{id}/abandon`      | Abandonner une partie                |

### Deroulement d'une partie

| Methode | URL                            | Description                          |
|---------|--------------------------------|--------------------------------------|
| GET     | `/api/games/{id}/board`        | Recuperer le plateau                 |
| GET     | `/api/games/{id}/active-player`| Recuperer le joueur actif            |
| POST    | `/api/games/{id}/move`         | Jouer un coup                        |

### Gestion des scores

| Methode | URL                              | Description                        |
|---------|----------------------------------|------------------------------------|
| GET     | `/api/scores/hall-of-fame`       | Classement general (Hall of Fame)  |
| GET     | `/api/scores/{id}`               | Detail d'un score                  |
| GET     | `/api/scores/player/{playerId}`  | Scores d'un joueur                 |
| GET     | `/api/scores/game/{gameId}`      | Scores d'une partie                |

---

## Regles du jeu implementees

1. **Deplacement** : un joueur deplace 1, 2 ou 3 billes alignees dans une des 6 directions
2. **Mouvement en ligne (inline)** : les billes avancent dans l'axe de leur alignement
3. **Mouvement lateral (broadside)** : les billes avancent perpendiculairement (pas de poussee)
4. **Poussee (sumito)** : une ligne peut pousser des billes adverses en superiorite numerique (3v2, 3v1, 2v1)
5. **Ejection** : une bille poussee hors du plateau est definitivement perdue
6. **Victoire** : le premier joueur a ejecter **6 billes adverses** gagne
7. **Abandon** : un joueur peut abandonner, l'adversaire gagne par forfait
8. **Tour par tour** : les noirs commencent, puis alternance

---

## Interface web

Accessible sur `http://localhost:8080`, le site propose :
- **Gestion des joueurs** : creer, modifier, supprimer des joueurs
- **Lancer une partie** : choisir deux joueurs
- **Plateau visuel** : plateau hexagonal avec selection de billes et directions
- **Abandon** : bouton pour abandonner la partie en cours
- **Hall of Fame** : classement general des joueurs

---

## Technologies

| Technologie       | Role                                            |
|-------------------|-------------------------------------------------|
| Java 17           | Langage de programmation                        |
| Spring Boot 3.2   | Framework web + injection de dependances        |
| Spring Web        | Couche REST (controllers, JSON)                 |
| Spring Data JPA   | ORM pour acces base de donnees                  |
| H2 Database       | Base de donnees en memoire (dev/test)           |
| Maven             | Gestion des dependances et build                |
