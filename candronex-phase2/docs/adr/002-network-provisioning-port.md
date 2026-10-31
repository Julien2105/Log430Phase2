# ADR-002 — Intégration réseau via NetworkProvisioningPort

**Statut** : Accepté

## Contexte
UC-06 nécessite l'activation de slices alors qu'aucun cœur 5G réel n'est fourni. Le domaine doit rester indépendant de l'infrastructure réseau.

## Décision
Le domaine de SliceService utilise le port NetworkProvisioningPort, implémenté par MockOpen5GSAdapter (latence MOCK_LATENCY_MIN_MS/MAX_MS, échecs MOCK_FAILURE_RATE).

## Conséquences
Domaine indépendant, tests simples; Open5GSAdapter remplacera le mock en Phase 3 sans toucher au domaine.

## Option écartée
Appeler Open5GS directement depuis le domaine.
