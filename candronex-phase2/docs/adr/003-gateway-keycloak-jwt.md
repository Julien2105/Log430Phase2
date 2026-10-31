# ADR-003 — API Gateway, Keycloak et validation du JWT

**Statut** : Accepté

## Contexte
L'énoncé impose une API Gateway, Keycloak et des JWT OAuth2 Client Credentials. Les clients ne doivent jamais joindre directement les microservices.

## Décision
KrakenD route les requêtes et transmet Authorization sans le modifier. Chaque microservice valide lui-même signature, expiration, issuer, audience candronex-api et la claim clientId, et filtre toutes ses requêtes par clientId.

## Conséquences
Sécurité homogène et isolation des clients; chaque service porte la validation.

## Option écartée
Validation du JWT uniquement à la passerelle (point de confiance unique).
