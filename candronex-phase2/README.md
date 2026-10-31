# CanDroneX — Phase 2 : microservices hexagonaux

Évolution du monolithe modulaire de la Phase 1 vers cinq microservices Spring Boot (Java 21), une passerelle KrakenD et Keycloak. Communication **REST synchrone uniquement** (aucun courtier de messages; l'événementiel est préparé pour la Phase 3).

## Architecture

| Service | Port | Base | Rôle |
|---|---|---|---|
| drone-service | 8081 | drone_db | UC-02. Drones; `droneId` unique par `(clientId, droneId)`; IMSI jamais exposé |
| order-service | 8082 | order_db | UC-04. `Order` / `OrderLine`, idempotence `(clientId, Idempotency-Key)`, snapshot des caractéristiques |
| slice-service | 8083 | slice_db | UC-06. Agrégat `Service` (PENDING, ACTIVATING, ACTIVE, FAILED), `ActivateServiceUseCase`, `NetworkProvisioningPort` → `MockOpen5GSAdapter` |
| inventory-service | 8084 | inventory_db | `ServiceInstance` (services effectivement activés) |
| catalog-service | 8085 | catalog_db | `ServiceOffering` → `ServiceSpecification` → `ServiceCharacteristic` (sst, sd, dnn, fiveQi) |
| gateway (KrakenD) | 8000 | — | Point d'entrée public |
| keycloak | 8080 | — | Realm `candronex`, OAuth2 Client Credentials |
| swagger-ui | 8089 | — | Documentation OpenAPI (`docs/openapi/`) |

Appels entre services (graphe sans cycle) : Order → Drone (`GET /network-identity`), Order → Catalog (`GET /service-offerings`), Order → Slice (`POST /services/activation`, lecture des états), Slice → Inventory (`PUT /services/{serviceId}`).
Chaque service suit l'architecture hexagonale : `api / application / domain / infrastructure`. Les ports de SliceService : `ActivateServiceUseCase`, `ConsultActivationQuery`, `ResumePendingActivationsUseCase` (entrée); `NetworkProvisioningPort`, `ServiceRepository`, `ServiceInventory`, `AuditTrail`, `Clock`, `ActivationScheduler` (sortie).

Seuls la passerelle (8000), Keycloak (8080) et Swagger UI (8089) sont publiés; les services et leurs bases restent sur le réseau interne `backend`.

## Démarrage

```bash
cp .env.example .env        # optionnel : valeurs par défaut utilisables en démonstration
docker compose up --build
./demo.sh                   # UC-02, UC-04, UC-06 via la passerelle (curl + jq)
```

## Sécurité

`Authorization: Bearer <JWT>` (aucun `X-Client-Id`). Chaque microservice valide la signature (JWKS Keycloak), l'expiration, l'issuer, l'audience `candronex-api` et la claim `clientId`, puis filtre toutes ses requêtes par `clientId`. Une ressource d'un autre client répond 404. Clients de démonstration : `client-a`, `client-b`, `slice-service` (technique). Les secrets viennent de `.env`, jamais du code.

## API (`/api/v1`)

Publiques : `POST/GET /drones`, `GET /drones/{droneId}`, `GET /service-offerings[/{id}]`, `POST/GET /orders`, `GET /orders/{orderId}`, `GET /services[/{serviceId}]`.
Internes (non exposées par la passerelle) : `GET /drones/{droneId}/network-identity`, `POST|GET /services/activation`, `PUT /services/{serviceId}`.
`POST /orders` exige `Idempotency-Key` : même clé et même contenu = même commande (200); contenu différent = 409. Le rejeu d'une activation utilise `serviceId`.
Erreurs : `application/problem+json` (RFC 9457) avec `type`, `title`, `status`, `detail`, `code`, `correlationId`.

## Mock du réseau 5G

`MOCK_LATENCY_MIN_MS=300`, `MOCK_LATENCY_MAX_MS=1500`, `MOCK_FAILURE_RATE=0` (0 à 100). Avec `MOCK_FAILURE_RATE=100`, tout service passe à `FAILED` et rien n'est écrit dans l'inventaire.

## Journalisation

Logs JSON : `timestamp`, `service`, `clientId`, `orderId`, `correlationId`. Événements : `ORDER_CREATED`, `SERVICE_ACTIVATED`, `SERVICE_FAILED`. Aucun IMSI ni jeton dans les logs.

## Tests et performance

```bash
mvn verify                  # tests unitaires et d'intégration (Testcontainers : Docker requis)
k6 run performance/load-test.js
psql -d slice_db -f performance/p95-activation.sql   # P95 acceptedAt -> activatedAt
```
Cibles Phase 2 : P95 ≤ 5 s et débit ≥ 600 opérations/s.

## Structure

```
drone-service/ catalog-service/ order-service/ slice-service/ inventory-service/
   └─ pom.xml, Dockerfile, src/main/{java,resources/{application.yml,db/migration}}, src/test
gateway/krakend.json    keycloak/candronex-realm.json
docs/adr/  docs/openapi/    performance/    docker-compose.yml  demo.sh
```
