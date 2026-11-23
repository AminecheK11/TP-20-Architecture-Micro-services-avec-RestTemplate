
# TP — Architecture Microservices avec Spring Cloud, Eureka, Gateway et RestTemplate

**Réalisé par : Amine CHEKKOURI**  
**Établissement : EMSI Marrakech**  
**Filière : Informatique et Réseaux — 5ème année**

---

##  Présentation du TP

Ce travail pratique consiste à concevoir et mettre en œuvre une architecture microservices distribuée à l'aide de **Spring Boot** et **Spring Cloud**.

L'objectif est de développer plusieurs services indépendants, d'assurer leur découverte automatique grâce à **Netflix Eureka**, de centraliser les requêtes avec **Spring Cloud Gateway** et d'établir une communication entre microservices à l'aide de **RestTemplate**.

L'architecture comprend quatre applications :

- **Eureka Server** : registre central de découverte des services.
- **SERVICE-CLIENT** : gestion des clients et de leurs informations.
- **Gateway** : passerelle API assurant le routage dynamique.
- **SERVICE-CAR** : gestion des voitures et récupération des informations des propriétaires.

## 🛠️ Technologies utilisées

| Technologie | Rôle |
|---|---|
| Java 17 | Développement des applications |
| Spring Boot 3.5.16 | Framework des microservices |
| Spring Cloud 2025.0.3 | Infrastructure distribuée |
| Netflix Eureka | Service Discovery |
| Spring Cloud Gateway WebFlux | Routage dynamique |
| Spring Data JPA / Hibernate | Persistance des données |
| MySQL | Bases de données relationnelles |
| RestTemplate | Communication HTTP interservices |
| Lombok | Simplification des classes Java |
| Maven | Compilation et gestion des dépendances |
| IntelliJ IDEA | Environnement de développement |

---

# A — Mise en place du service Discovery Eureka

## 1. Objectif

Le serveur Eureka est un composant central dans une architecture microservices. Il permet aux applications de s'enregistrer automatiquement et de découvrir les autres services disponibles.

Ce mécanisme évite de définir manuellement les adresses réseau de chaque microservice et facilite leur intégration dans un système distribué.

## 2. Création du projet Spring Boot

Le projet `eureka-server` a été créé avec Spring Boot et la dépendance **Eureka Server**.

La classe principale `EurekaServerApplication` utilise les annotations :

- `@SpringBootApplication` : initialisation de l'application Spring Boot.
- `@EnableEurekaServer` : activation du serveur de découverte.

## 3. Configuration du serveur

Le fichier `application.yml` définit les paramètres nécessaires au fonctionnement du serveur Eureka.

```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false

logging:
  level:
    com.netflix.eureka: OFF
    com.netflix.discovery: OFF
```

Le serveur est configuré pour fonctionner comme un registre indépendant, sans s'enregistrer lui-même comme client Eureka.

## 4. Exécution et vérification

Après le démarrage de l'application, le tableau de bord Eureka est accessible à l'adresse :

**http://localhost:8761/**

### 📸 Capture 01 — Tableau de bord Eureka

<img width="1279" height="661" alt="01_Eureka_Dashboard" src="https://github.com/user-attachments/assets/4816b58a-67a2-4746-8964-38497e5abf09" />


*Figure 1 : Interface du serveur Eureka lors de son premier démarrage.*

---

# B — Développement du microservice Client

## 1. Objectif

Le microservice **SERVICE-CLIENT** est chargé de gérer les informations des clients.

Il propose une API REST permettant de consulter les clients, de rechercher un client à partir de son identifiant et d'enregistrer de nouvelles données.

Les informations sont persistées dans une base de données MySQL nommée `clientservicedb`.

## 2. Configuration du microservice

Le service a été développé avec les dépendances suivantes :

- Spring Web
- Spring Data JPA
- MySQL Driver
- Eureka Discovery Client
- Lombok

Le fichier `application.yml` définit le port **8081**, le nom logique `SERVICE-CLIENT`, la connexion à MySQL et l'adresse du serveur Eureka.

## 3. Implémentation des composants

### Entité Client

La classe `Client` représente les informations d'un client. Elle contient les attributs `id`, `nom` et `age`.

Les annotations JPA assurent la correspondance entre les objets Java et les enregistrements de la base de données.

### ClientRepository

L'interface `ClientRepository` hérite de `JpaRepository` afin de fournir les opérations de persistance.

### ClientService

La classe `ClientService` centralise la logique métier et assure la communication avec la couche de persistance.

### ClientController

Le contrôleur REST expose les endpoints permettant d'interagir avec le microservice à travers des requêtes HTTP.

## 4. Tests des endpoints REST

### 📸 Capture 02 — Liste des clients

**Requête :** `GET http://localhost:8081/api/client`

<img width="277" height="280" alt="02_Client_Liste" src="https://github.com/user-attachments/assets/9024610d-39e2-4b18-a48a-50d2b0fa70d0" />


*Figure 2 : Consultation des clients depuis l'API REST du microservice.*

### 📸 Capture 03 — Détail d'un client

**Requête :** `GET http://localhost:8081/api/client/1`

<img width="313" height="162" alt="03_Client_Detail" src="https://github.com/user-attachments/assets/12dafa4d-e229-4f5a-8e90-3f7f5d3381f5" />


*Figure 3 : Recherche d'un client par son identifiant.*

## 5. Intégration avec Eureka

Le microservice Client est configuré pour s'enregistrer automatiquement auprès du serveur Eureka.

Cette intégration permet aux autres composants de l'architecture de le découvrir à partir de son nom logique.

### 📸 Capture 04 — Enregistrement de SERVICE-CLIENT

<img width="1265" height="692" alt="04_Eureka_Service_Client" src="https://github.com/user-attachments/assets/b4035703-6979-4edd-8c65-5f9d52c473ba" />


*Figure 4 : Enregistrement du microservice Client dans le registre Eureka.*

---

# C — Mise en place de Spring Cloud Gateway

## 1. Objectif

Spring Cloud Gateway joue le rôle de point d'entrée centralisé pour les requêtes HTTP.

Elle permet de rediriger les demandes vers les microservices concernés, en utilisant les informations disponibles dans le registre Eureka.

Cette architecture évite aux consommateurs des API de devoir connaître directement les adresses et les ports des différents services.

## 2. Création et configuration

Le projet `gateway` utilise **Spring Cloud Gateway WebFlux** et **Eureka Discovery Client**.

La passerelle fonctionne sur le port **8888** et utilise le mécanisme **Discovery Locator** pour générer automatiquement les routes des services autorisés.

Le routage repose sur les identifiants logiques des microservices et utilise Spring Cloud LoadBalancer.

## 3. Fonctionnement du routage dynamique

Lorsqu'une requête arrive à la Gateway, celle-ci identifie le service demandé et utilise les informations fournies par Eureka pour déterminer sa destination.

Le préfixe correspondant au nom du service est supprimé avant la transmission de la requête au microservice.

Ce fonctionnement permet de centraliser les accès tout en conservant l'indépendance des applications métier.

## 4. Vérification dans Eureka

### 📸 Capture 05 — Enregistrement de la Gateway

<img width="1280" height="722" alt="05_Eureka_Gateway" src="https://github.com/user-attachments/assets/879f6973-b6d2-4a2d-a316-f4e77307c10b" />


*Figure 5 : Enregistrement de la Gateway et du microservice Client auprès d'Eureka.*

## 5. Tests du routage vers SERVICE-CLIENT

### 📸 Capture 06 — Liste des clients via Gateway

**Requête :** `GET http://localhost:8888/SERVICE-CLIENT/api/client`

<img width="355" height="266" alt="06_Gateway_Client_Liste" src="https://github.com/user-attachments/assets/8e061da8-d134-4c52-9245-08f64b578947" />


*Figure 6 : Consultation des clients à travers la passerelle API.*

### 📸 Capture 07 — Détail d'un client via Gateway

**Requête :** `GET http://localhost:8888/SERVICE-CLIENT/api/client/1`

<img width="478" height="167" alt="07_Gateway_Client_Detail" src="https://github.com/user-attachments/assets/dd411e20-5bf1-42c7-9962-4d51b372c1d4" />



*Figure 7 : Routage d'une requête ciblant un client spécifique.*

## 6. Vérification des routes dynamiques

Spring Boot Actuator permet de consulter les routes actuellement générées par la Gateway.

### 📸 Capture 08 — Routes de la Gateway

**Requête :** `GET http://localhost:8888/actuator/gateway/routes`

<img width="773" height="289" alt="08_Gateway_Routes" src="https://github.com/user-attachments/assets/992c01ad-cfa2-4766-adaf-35d8b47e928f" />


*Figure 8 : Inspection des routes dynamiques générées à partir du registre Eureka.*

## 7. Vérification de l'état de santé

L'endpoint Actuator Health fournit un mécanisme de surveillance de la disponibilité de la passerelle.

### 📸 Capture 09 — État de santé de la Gateway

**Requête :** `GET http://localhost:8888/actuator/health`

<img width="284" height="115" alt="09_Gateway_Health" src="https://github.com/user-attachments/assets/a015d555-ddb3-48ae-a43b-ea55cd881a49" />


*Figure 9 : Vérification de la disponibilité de Spring Cloud Gateway.*

---

# D — Développement du microservice Car avec RestTemplate

## 1. Objectif

Le microservice **SERVICE-CAR** permet de gérer les voitures et leurs associations avec les clients.

Chaque voiture possède un identifiant de propriétaire. Les informations détaillées de ce dernier sont récupérées depuis SERVICE-CLIENT grâce à une communication HTTP utilisant **RestTemplate**.

Cette conception permet de maintenir des microservices indépendants, chacun responsable de ses propres données.

## 2. Configuration du microservice

Le service Car fonctionne sur le port **8082** et utilise une base de données MySQL indépendante nommée `carservicedb`.

Il est enregistré dans Eureka sous le nom `SERVICE-CAR`.

Les principales dépendances utilisées sont Spring Web, Spring Data JPA, MySQL Driver, Eureka Discovery Client et Lombok.

## 3. Implémentation des composants

### Entité Car

L'entité `Car` contient les informations relatives à une voiture :

- Identifiant
- Marque
- Modèle
- Matricule
- Identifiant du client propriétaire

### Modèle Client

La classe `Client` représente les informations du propriétaire récupérées depuis le microservice Client.

Il s'agit d'un modèle de transfert de données et non d'une entité persistée dans la base de données du service Car.

### Modèle CarResponse

La classe `CarResponse` regroupe les informations d'une voiture et les données de son propriétaire dans une réponse REST unique.

### CarRepository

Le repository assure la persistance des voitures à travers Spring Data JPA.

### CarService

La couche service implémente la logique métier et la communication avec SERVICE-CLIENT à l'aide de RestTemplate.

### CarController

Le contrôleur expose les endpoints REST permettant de consulter la liste des voitures et de récupérer une voiture par identifiant.

## 4. Communication interservices avec RestTemplate

Le microservice Car utilise RestTemplate pour envoyer des requêtes HTTP au microservice Client à travers la Gateway.

L'URL utilisée suit le format :

```text
http://localhost:8888/SERVICE-CLIENT/api/client/{id}
```

Le fonctionnement repose sur les étapes suivantes :

1. SERVICE-CAR récupère les informations de la voiture depuis sa base MySQL.
2. Il utilise l'identifiant du propriétaire pour effectuer un appel HTTP avec RestTemplate.
3. La Gateway redirige la requête vers SERVICE-CLIENT grâce au registre Eureka.
4. SERVICE-CAR construit une réponse contenant les informations de la voiture et de son propriétaire.

Cette communication évite les relations JPA directes entre les bases de données des microservices.

## 5. Enregistrement dans Eureka

### 📸 Capture 10 — Enregistrement de SERVICE-CAR

<img width="1270" height="734" alt="10_Eureka_Service_Car" src="https://github.com/user-attachments/assets/b52e12ad-a767-4272-91e4-60feae1d9570" />


*Figure 10 : Tableau de bord Eureka présentant Gateway, SERVICE-CLIENT et SERVICE-CAR.*

## 6. Tests directs du microservice Car

### 📸 Capture 11 — Liste des voitures

**Requête :** `GET http://localhost:8082/api/car`

<img width="274" height="607" alt="11_Car_Liste" src="https://github.com/user-attachments/assets/bdf90e8b-846c-4e76-9b50-e2b3a0429890" />


*Figure 11 : Consultation des voitures avec les informations de leurs propriétaires.*

### 📸 Capture 12 — Détail d'une voiture

**Requête :** `GET http://localhost:8082/api/car/1`

<img width="258" height="224" alt="12_Car_Detail" src="https://github.com/user-attachments/assets/6557765d-9edf-4c1a-8e69-14991e11be13" />


*Figure 12 : Consultation d'une voiture spécifique et de son propriétaire.*

## 7. Tests du microservice Car via Gateway

### 📸 Capture 13 — Liste des voitures via Gateway

**Requête :** `GET http://localhost:8888/SERVICE-CAR/api/car`

<img width="370" height="620" alt="13_Gateway_Car_Liste" src="https://github.com/user-attachments/assets/b9e969e4-587a-4cab-ab92-02002386c216" />


*Figure 13 : Récupération des voitures à travers le routage dynamique de la Gateway.*

### 📸 Capture 14 — Détail d'une voiture via Gateway

**Requête :** `GET http://localhost:8888/SERVICE-CAR/api/car/1`

<img width="370" height="244" alt="14_Gateway_Car_Detail" src="https://github.com/user-attachments/assets/0c2840f3-e0eb-4a10-a3c0-6087356e075f" />


*Figure 14 : Consultation d'une voiture par identifiant à travers la Gateway.*

---

# Tests et validation

Les applications ont été compilées et testées à l'aide de Maven.

| Application | Tests automatisés | Compilation |
|---|---:|---|
| Eureka Server | 4 | BUILD SUCCESS |
| SERVICE-CLIENT | 7 | BUILD SUCCESS |
| Gateway | 6 | BUILD SUCCESS |
| SERVICE-CAR | 6 | BUILD SUCCESS |

Les vérifications ont porté sur :

- Le démarrage des applications Spring Boot.
- La connexion aux bases de données MySQL.
- La persistance et la consultation des données.
- L'enregistrement des microservices dans Eureka.
- Le routage dynamique des requêtes HTTP.
- La communication interservices via RestTemplate.
- La gestion des ressources inexistantes.
- Le comportement du service Car lorsque le service Client est indisponible.

---

# Conclusion

Ce travail pratique a permis de mettre en œuvre une architecture microservices distribuée reposant sur **Spring Boot, Spring Cloud Eureka, Spring Cloud Gateway et RestTemplate**.

L'utilisation d'Eureka facilite la découverte automatique des services, tandis que la Gateway centralise le routage des requêtes HTTP.

Les microservices Client et Car disposent de bases de données MySQL distinctes, ce qui garantit une séparation des responsabilités et une meilleure indépendance des composants.

La communication interservices réalisée avec RestTemplate permet d'enrichir les réponses du microservice Car avec les informations provenant du microservice Client.

Cette architecture constitue une base technique pour le développement de systèmes distribués modulaires, maintenables et évolutifs.

---

**Réalisé par : Amine CHEKKOURI**  
**EMSI Marrakech — Informatique et Réseaux**
