# ADR-001 — Découpage en microservices et propriété des données

**Statut** : Accepté

## Contexte
La Phase 2 impose une architecture microservices alignée sur les Bounded Contexts. Chaque service doit être autonome et propriétaire de ses données.

## Décision
Cinq microservices : DroneService, CatalogService, OrderService, SliceService, InventoryService. Chaque service possède sa base PostgreSQL (drone_db, catalog_db, order_db, slice_db, inventory_db) et les échanges se font par API REST avec des DTO dédiés.

## Conséquences
Faible couplage, évolution indépendante, préparation à la Phase 3. La cohérence entre bases est gérée par identifiants logiques et rejeu idempotent.

## Option écartée
Base partagée entre services (fort couplage, contraire à la Phase 2).
