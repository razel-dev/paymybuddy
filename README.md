# PayMyBuddy — Backend (MVC + Thymeleaf)

Application Spring Boot (Java 21) pour la gestion d’utilisateurs, comptes, connexions entre utilisateurs (buddies), transactions entre utilisateurs et virements bancaires, rendue côté serveur via des contrôleurs Spring MVC et des vues Thymeleaf. Persistance MySQL, migrations Flyway, tests d’intégration avec Testcontainers (MySQL).

## Sommaire
- Aperçu
- Architecture
- Pile technologique
- Prérequis
- Démarrage rapide
- Configuration
- Base de données et migrations
- Profils et données de démo
- Navigation et pages
- Gestion des erreurs
- Sécurité
- Tests (MySQL via Docker/Testcontainers)
- Packaging et déploiement
- Docker (run local)
- Points d’extension
- Commandes utiles
- Dépannage
- Licence

## Aperçu

- Rendu côté serveur: Spring MVC + Thymeleaf.
- Persistance: Spring Data JPA (Jakarta) + Hibernate.
- Base: MySQL (locale ou Docker), schéma versionné avec Flyway.
- Validation: Jakarta Validation.
- Tests d’intégration: MySQL éphémère via Testcontainers.

Flux serveur:
Controller (Spring MVC) -> Services -> Repositories -> JPA/Hibernate -> MySQL -> Vues Thymeleaf.

## Architecture

- model: entités JPA (utilisateurs, comptes, transactions, virements, connexions)
- repository: interfaces Spring Data JPA
- service/crud (+ impl): logique métier, transactions
- web:
  - controller: contrôleurs MVC (retournent des noms de templates Thymeleaf)
  - dto: DTO de formulaires et de vues
  - mapper: mappers DTO <-> entités (MapStruct)
  - exception: gestion des erreurs
- security: composants de sécurité (authentification/autorisation)
- infra: configuration, initialisation (données de démo)

## Pile technologique

- Java 21
- Spring Boot 3.5.x (Web/MVC, Validation, Data JPA, Security, Thymeleaf)
- Hibernate (Jakarta Persistence)
- MySQL (runtime) + Flyway (migrations)
- Lombok (réduction du boilerplate)
- MapStruct (mappers compile-time)
- Testcontainers (MySQL pour tests d’intégration)
- Build: Maven

Pas de doc OpenAPI/Swagger car absence d’API REST en V1

## Prérequis

- JDK 21
- Maven 3.9+
- MySQL local (ou Docker)
- Docker en fonctionnement pour exécuter les tests d’intégration (Testcontainers)

## Démarrage rapide

1) Créer la base:
- CREATE DATABASE paymybuddy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

2) Variables d’environnement (facultatives):
- DB_USERNAME et DB_PASSWORD (par défaut: paymybuddy/paymybuddy)
- ou override complet via SPRING_DATASOURCE_URL/USERNAME/PASSWORD

3) Lancer l’application:
- mvn spring-boot:run
- Ouvrir le navigateur sur http://localhost:8080 et naviguer via les pages Thymeleaf.

## Configuration

- Datasource:
  - jdbc:mysql://127.0.0.1:3306/paymybuddy
  - username/password via variables d’environnement (avec valeurs par défaut)
- JPA:
  - spring.jpa.hibernate.ddl-auto=validate (schéma géré par Flyway)
  - spring.jpa.open-in-view=false
  - show-sql/format-sql activés pour le dev
- Flyway:
  - enabled=true
  - locations=classpath:db/migration
  - baseline-on-migrate=true (utile si base déjà existante non versionnée)
- Thymeleaf:
  - templates sous classpath:/templates/

## Base de données et migrations

- Migrations Flyway (Vx__*.sql) exécutées au démarrage.
- ddl-auto=validate empêche les dérives de schéma: corriger via migrations.

## Profils et données de démo

- Profil demo:
  - mvn spring-boot:run -Dspring-boot.run.profiles=demo.
  - Crée des utilisateurs de démonstration.

## Navigation et pages

L’application suit un modèle MVC classique. Exemples de routes/pages:
- Comptes:
  - GET /accounts — liste des comptes de l’utilisateur courant, formulaire de création
  - POST /accounts — création d’un compte (formulaire Thymeleaf)
  - POST /accounts/{id}/delete — suppression d’un compte puis redirection
- Transferts:
  - GET /transfer — formulaire de virement entre comptes + historique du compte sélectionné
  - POST /transfer — soumission du virement puis redirection
- Connexions (buddies):
  - GET /connections — liste des connexions + formulaire d’ajout par e-mail
  - POST /connections — ajout d’une connexion, messages d’erreur/info rendus dans la page

Les contrôleurs utilisent Principal pour récupérer l’utilisateur courant, puis injectent les DTO et messages de validation dans le modèle.

## Gestion des erreurs

- Format Problem Details activé pour les erreurs HTTP.
- Les erreurs de validation sont renvoyées aux vues Thymeleaf pour affichage.

## Sécurité

- Base Security Spring activée.
- Rate limiting basique configurable (nombre max de requêtes / fenêtre).
- Authentification/autorisation à adapter selon besoins.

## Tests (MySQL via Docker/Testcontainers)

- Lancer:
  - mvn test
- Ce qui se passe:
  - Un conteneur MySQL éphémère est démarré automatiquement par Testcontainers.
  - Le datasource de test est injecté sans configuration manuelle.
- Rapports:
  - target/surefire-reports

Dépannage tests:
- Docker doit être démarré; sinon les tests d’intégration échouent.
- Pour n’exécuter que des tests unitaires, utilisez un profil Maven/filtrage de classes.

## Packaging et déploiement

- Build:
  - mvn clean package
  - Jar: target/paymybuddy-<version>.jar
- Exécution:
  - java -jar target/paymybuddy-<version>.jar
- Variables utiles:
  - SPRING_PROFILES_ACTIVE
  - DB_USERNAME / DB_PASSWORD
  - SPRING_DATASOURCE_URL / USERNAME / PASSWORD

## Docker (run local)

Exécuter un MySQL local:
- docker run --name mysql8-paymybuddy -e MYSQL_DATABASE=paymybuddy -e MYSQL_USER=paymybuddy -e MYSQL_PASSWORD=paymybuddy -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8.4

L’application utilisera 127.0.0.1:3306/paymybuddy avec les identifiants fournis.

## Points d’extension

- Vues:
  - Ajouter des templates Thymeleaf sous templates/
- Contrôleurs:
  - Ajouter de nouvelles routes/pages
- Services/Repositories:
  - Étendre la logique métier et les requêtes
- Sécurité:
  - Brancher votre stratégie (form login, JWT pour APIs internes futures, etc.)
- Observabilité:
  - Ajouter Actuator/metrics/logging avancé si nécessaire

## Commandes utiles

- Lancer en dev:
  - mvn spring-boot:run
- Build + tests:
  - mvn clean verify

## Dépannage

- Erreurs Flyway (checksum/out-of-order)
    - Ne modifie jamais une migration déjà appliquée: crée une nouvelle (Vx+1).
    - Vérifie l’ordre/numérotation des scripts dans db/migration.
    - Base existante non versionnée: active baseline-on-migrate=true (avec prudence).

- ddl-auto=validate échoue
    - Le schéma ne correspond pas aux entités JPA.
    - Ajoute une migration Vx+1 pour aligner la base (ou recrée la base en dev).

- Connexion MySQL
    - Vérifie l’URL JDBC, l’utilisateur/mot de passe et que MySQL écoute bien sur le port prévu.

- Docker non lancé (tests)
    - Démarre Docker avant mvn test.


